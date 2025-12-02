package de.melanx.maledicta;

import de.melanx.maledicta.capabilities.EnergyCollector;
import de.melanx.maledicta.data.DamageTypeProvider;
import de.melanx.maledicta.data.EnchantmentProvider;
import de.melanx.maledicta.lightning.ColoredLightningBoltEntity;
import de.melanx.maledicta.lightning.ColoredLightningBoltRenderer;
import de.melanx.maledicta.registration.ModBlocks;
import de.melanx.maledicta.registration.ModDataComponentTypes;
import de.melanx.maledicta.registration.ModEntities;
import de.melanx.maledicta.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = "maledicta")
public class EventListener {

    private static long nextTime = 0L;

    @SubscribeEvent
    public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.lightningBolt, ColoredLightningBoltRenderer::new);
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
                        ColoredLightningBoltEntity entity = ModEntities.lightningBolt.create(player.level());
                        //noinspection ConstantConditions
                        entity.setColor(0xFF0000);
                        entity.moveTo(player.position());
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

        if (event.getLevel().isClientSide) {
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
            RandomSource random = causer.level().random;
            LivingEntity victim = event.getEntity();

            if (ModConfig.NegativeEnergy.enabled && victim.getType().is(EntityTypeTags.UNDEAD)) {
                ItemStack mainHandItem = causer.getMainHandItem();
                if (Util.isEnchantable(mainHandItem)) {
                    mainHandItem.update(ModDataComponentTypes.energyCollector, EnergyCollector.EMPTY, energyCollector -> {
                        energyCollector.addEnergy(ModConfig.NegativeEnergy.hurtAddition);
                        return energyCollector;
                    });
                }
            }

            Registry<Enchantment> enchantments = victim.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            Optional<Holder.Reference<Enchantment>> karma = enchantments.getHolder(EnchantmentProvider.KARMA);
            Optional<Holder.Reference<Enchantment>> kindness = enchantments.getHolder(EnchantmentProvider.KINDNESS);

            karma.ifPresent(key -> {
                boolean receiveKarma = Util.hasEnchantmentInHand(causer, karma.get());
                if (receiveKarma && causer.getRandom().nextDouble() <= ModConfig.karmaChance) {
                    causer.hurt(new DamageSource(causer.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypeProvider.KARMA)), event.getAmount());
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
        Holder.Reference<Enchantment> fleetingRiches = event.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(EnchantmentProvider.FLEETING_RICHES);
        if (event.getTool().getEnchantmentLevel(fleetingRiches) <= 0) {
            return;
        }

        double chance = 0.2;
        for (ItemEntity drop : event.getDrops()) {
            if (event.getLevel().random.nextDouble() <= chance) {
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
        ColoredLightningBoltEntity coloredLightning = ModEntities.lightningBolt.create(event.getLevel());

        Objects.requireNonNull(coloredLightning).setDamage(0);
        coloredLightning.setVisualOnly(ModConfig.safeLightnings);
        coloredLightning.moveTo(lightning.position());
        coloredLightning.setColor(Util.LIGHTNING_COLOR);

        event.getLevel().addFreshEntity(coloredLightning);
    }

    private static List<ItemStack> getHandOrArmorItems(Player player) {
        List<ItemStack> items = new ArrayList<>();
        items.add(player.getMainHandItem());
        items.add(player.getOffhandItem());
        items.addAll((Collection<? extends ItemStack>) player.getArmorSlots());
        return items.removeIf(ItemStack::isEmpty) ? items : Collections.emptyList();
    }
}
