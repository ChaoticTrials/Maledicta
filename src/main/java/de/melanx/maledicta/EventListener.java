package de.melanx.maledicta;

import de.melanx.maledicta.capabilities.EnergyCollector;
import de.melanx.maledicta.data.DamageTypeProvider;
import de.melanx.maledicta.data.EnchantmentProvider;
import de.melanx.maledicta.lightning.ColoredLightningBoltEntity;
import de.melanx.maledicta.lightning.ColoredLightningBoltRenderer;
import de.melanx.maledicta.lightning.LightningArcRenderer;
import de.melanx.maledicta.network.ModNetwork;
import de.melanx.maledicta.registration.ModBlocks;
import de.melanx.maledicta.registration.ModDataComponentTypes;
import de.melanx.maledicta.registration.ModEntities;
import de.melanx.maledicta.registration.ModSoundEvents;
import de.melanx.maledicta.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = "maledicta")
public class EventListener {

    private static final int DOOM_HEALTH = 10;
    private static final int DOOM_COUNTDOWN = 200;

    private static long nextTime = 0L;
    public static final Map<UUID, Long> DOOMED_PLAYERS = new HashMap<>();

    @SubscribeEvent
    public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.lightningBolt, ColoredLightningBoltRenderer::new);
        event.registerEntityRenderer(ModEntities.lightningArc, LightningArcRenderer::new);
    }

    @SubscribeEvent
    public static void onEntityTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        UUID playerId = player.getUUID();
        long gameTime = player.level().getGameTime();
        Long deadline = DOOMED_PLAYERS.get(playerId);

        if (player.isAlive() && player.getHealth() <= DOOM_HEALTH && EventListener.hasCursedArmor(player, EnchantmentProvider.DOOM)) {
            if (deadline == null) {
                DOOMED_PLAYERS.put(playerId, gameTime + DOOM_COUNTDOWN);
                player.level().playSound(null, player, ModSoundEvents.heartbeat, SoundSource.PLAYERS, 1, 1);
                ModNetwork.heartbeatEffect(player, true);
            } else if (deadline <= gameTime) {
                DOOMED_PLAYERS.remove(playerId);
                ModNetwork.heartbeatEffect(player, false);
                player.hurt(player.damageSources().source(DamageTypeProvider.DOOMED), player.getMaxHealth());
            }
        } else if (deadline != null) {
            EventListener.stopDoom(player);
        }
    }

    @SubscribeEvent
    public static void onStrifeTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator() || player.isCreative() || player.getRandom().nextDouble() >= ModConfig.Strife.chance) {
            return;
        }

        if (!EventListener.hasCurseVisible(player, EnchantmentProvider.STRIFE)) {
            return;
        }

        Holder<Enchantment> curse = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(EnchantmentProvider.STRIFE);
        int strifePieces = 0;
        for (ItemStack stack : EventListener.visibleItems(player)) {
            if (stack.getEnchantmentLevel(curse) > 0) {
                strifePieces++;
            }
        }

        int radius = ModConfig.Strife.radius + (strifePieces - 1) * 2;
        List<Mob> mobs = player.level().getEntitiesOfClass(
                Mob.class,
                player.getBoundingBox().inflate(radius),
                mob -> mob instanceof Enemy && mob.getTarget() == null && mob.canAttack(player) && (!ModConfig.Strife.requireLineOfSight || mob.getSensing().hasLineOfSight(player))
        );

        for (Mob mob : mobs) {
            mob.setTarget(player);
            if (mob instanceof NeutralMob neutral) {
                neutral.setPersistentAngerTarget(EntityReference.of(player.getUUID()));
                neutral.startPersistentAngerTimer();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && DOOMED_PLAYERS.containsKey(player.getUUID())) {
            EventListener.stopDoom(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        DOOMED_PLAYERS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        if (!ModConfig.NegativeEnergy.enabled) {
            return;
        }

        long gameTime = event.getServer().overworld().getGameTime();

        // Initialize this.nextTime
        if (nextTime == 0L) {
            RandomSource random = RandomSource.create();
            nextTime = gameTime + random.nextIntBetweenInclusive(ModConfig.minCurseDelay, ModConfig.maxCurseDelay);
            return;
        }

        if (nextTime > gameTime) {
            return;
        }

        RandomSource random = RandomSource.create();
        nextTime = gameTime + random.nextIntBetweenInclusive(ModConfig.minCurseDelay, ModConfig.maxCurseDelay);

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            for (ItemStack stack : EventListener.getHandOrArmorItems(player)) {
                if (stack.isEmpty()) {
                    continue;
                }

                EnergyCollector energyCollector = stack.get(ModDataComponentTypes.energyCollector);
                if (energyCollector == null) {
                    continue;
                }

                if (energyCollector.negativeEnergy().test()) {
                    if (Util.tryToApplyCurse(player, stack)) {
                        // summon custom lightning
                        ColoredLightningBoltEntity entity = ModEntities.lightningBolt.create(player.level(), EntitySpawnReason.EVENT);
                        //noinspection ConstantConditions
                        entity.setColor(0xFF0000);
                        entity.snapTo(player.position());
                        entity.setVisualOnly(true);
                        player.level().addFreshEntity(entity);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!ModConfig.NegativeEnergy.enabled) {
            return;
        }

        if (event.getLevel().isClientSide()) {
            return;
        }

        if (event.getLevel().dimension() != Level.NETHER) {
            return;
        }

        // run this only once a second
        if (event.getLevel().getGameTime() % 20 != 0) {
            return;
        }

        MinecraftServer server = event.getLevel().getServer();
        //noinspection ConstantConditions
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (event.getLevel() == player.level() && player.level() == server.getLevel(Level.NETHER)) {
                for (ItemStack stack : EventListener.getHandOrArmorItems(player)) {
                    stack.update(ModDataComponentTypes.energyCollector, EnergyCollector.EMPTY, energyCollector -> {
                        energyCollector.addEnergy(ModConfig.NegativeEnergy.netherAddition);
                        return energyCollector;
                    });
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDealDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity causer) {
            RandomSource random = causer.level().getRandom();
            LivingEntity victim = event.getEntity();

            if (ModConfig.NegativeEnergy.enabled && victim.is(EntityTypeTags.UNDEAD)) {
                ItemStack mainHandItem = causer.getMainHandItem();
                if (Util.isEnchantable(mainHandItem)) {
                    mainHandItem.update(ModDataComponentTypes.energyCollector, EnergyCollector.EMPTY, energyCollector -> {
                        energyCollector.addEnergy(ModConfig.NegativeEnergy.hurtAddition);
                        return energyCollector;
                    });
                }
            }

            Registry<Enchantment> enchantments = victim.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            Optional<Holder.Reference<Enchantment>> karma = enchantments.get(EnchantmentProvider.KARMA);
            Optional<Holder.Reference<Enchantment>> kindness = enchantments.get(EnchantmentProvider.KINDNESS);

            karma.ifPresent(key -> {
                boolean receiveKarma = Util.hasEnchantmentInHand(causer, karma.get());
                if (receiveKarma && causer.getRandom().nextDouble() <= ModConfig.karmaChance) {
                    causer.hurt(new DamageSource(causer.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeProvider.KARMA)), event.getAmount());
                }
            });

            kindness.ifPresent(key -> {
                boolean healDontHurt = Util.hasEnchantmentInHand(causer, kindness.get());
                if (healDontHurt && causer.getRandom().nextDouble() <= ModConfig.kindnessChance) {
                    victim.heal(event.getAmount());
                    ((ServerLevel) victim.level()).sendParticles(ParticleTypes.HEART, victim.getRandomX(1), victim.getRandomY() + 0.5, victim.getRandomZ(1), 10, 0, 0, 0, random.nextGaussian() * 0.02D);
                    event.setCanceled(true);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        Holder.Reference<Enchantment> fleetingRiches = event.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(EnchantmentProvider.FLEETING_RICHES);
        if (event.getTool().getEnchantmentLevel(fleetingRiches) <= 0) {
            return;
        }

        double chance = 0.2;
        for (ItemEntity drop : event.getDrops()) {
            if (event.getLevel().getRandom().nextDouble() <= chance) {
                drop.setNeverPickUp();
                drop.lifespan = 30;
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity().getType() != EntityType.LIGHTNING_BOLT) {
            return;
        }

        LightningBolt lightning = (LightningBolt) event.getEntity();
        BlockPos strikePosition = lightning.getStrikePosition();
        if (!lightning.level().getBlockState(strikePosition).is(ModBlocks.maledictusAufero)) {
            return;
        }

        event.setCanceled(true);
        ColoredLightningBoltEntity coloredLightning = ModEntities.lightningBolt.create(event.getLevel(), EntitySpawnReason.EVENT);

        Objects.requireNonNull(coloredLightning).setDamage(0);
        coloredLightning.setVisualOnly(ModConfig.safeLightnings);
        coloredLightning.snapTo(lightning.position());
        coloredLightning.setColor(Util.LIGHTNING_COLOR);

        event.getLevel().addFreshEntity(coloredLightning);
    }

    private static Iterable<ItemStack> visibleItems(Player player) {
        List<ItemStack> stacks = new ArrayList<>();
        for (EquipmentSlot equipmentSlot : Util.WORN_OR_HELD) {
            stacks.add(player.getItemBySlot(equipmentSlot));
        }

        return stacks;
    }

    private static boolean hasCurseVisible(Player player, ResourceKey<Enchantment> curse) {
        return EventListener.hasCurse(player, EventListener.visibleItems(player), curse);
    }

    private static boolean hasCurseInHand(Player player, ResourceKey<Enchantment> curse) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(player.getMainHandItem());
        stacks.add(player.getOffhandItem());

        return EventListener.hasCurse(player, stacks, curse);
    }

    private static boolean hasCursedArmor(Player player, ResourceKey<Enchantment> cursed) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(player.getItemBySlot(EquipmentSlot.FEET));
        stacks.add(player.getItemBySlot(EquipmentSlot.LEGS));
        stacks.add(player.getItemBySlot(EquipmentSlot.CHEST));
        stacks.add(player.getItemBySlot(EquipmentSlot.HEAD));

        return EventListener.hasCurse(player, stacks, cursed);
    }

    private static boolean hasCurse(Player player, Iterable<ItemStack> stacks, ResourceKey<Enchantment> curse) {
        Holder.Reference<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(curse);
        for (ItemStack stack : stacks) {
            if (stack.getEnchantmentLevel(holder) > 0) {
                return true;
            }
        }

        return false;
    }

    private static void stopDoom(ServerPlayer player) {
        DOOMED_PLAYERS.remove(player.getUUID());
        ModNetwork.heartbeatEffect(player, false);
        player.connection.send(new ClientboundStopSoundPacket(ModSoundEvents.heartbeat.location(), SoundSource.PLAYERS));
    }

    private static List<ItemStack> getHandOrArmorItems(Player player) {
        List<ItemStack> items = new ArrayList<>();
        EventListener.visibleItems(player).forEach(items::add);

        return items.removeIf(ItemStack::isEmpty) ? items : Collections.emptyList();
    }
}
