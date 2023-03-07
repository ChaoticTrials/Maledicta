package de.melanx.maledicta;

import de.melanx.maledicta.capabilities.EnergyCollector;
import de.melanx.maledicta.capabilities.EnergyCollectorImpl;
import de.melanx.maledicta.lightning.ColoredLightningBoltRenderer;
import de.melanx.maledicta.lightning.LightningHelper;
import de.melanx.maledicta.registration.ModBlocks;
import de.melanx.maledicta.registration.ModDamageSources;
import de.melanx.maledicta.registration.ModEnchantments;
import de.melanx.maledicta.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class EventListener {

    private long nextTime = 0L;

    @SubscribeEvent
    public void attachItemCapability(AttachCapabilitiesEvent<ItemStack> event) {
        if (Util.isEnchantable(event.getObject())) {
            event.addCapability(Maledicta.getInstance().resource("energy_collector"), new EnergyCollectorImpl());
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (!ModConfig.NegativeEnergy.enabled) {
            return;
        }

        long gameTime = event.getServer().overworld().getGameTime();

        // Initialize this.nextTime
        if (this.nextTime == 0L) {
            RandomSource random = RandomSource.create();
            this.nextTime = gameTime + random.nextIntBetweenInclusive(ModConfig.minCurseDelay, ModConfig.maxCurseDelay);
            return;
        }

        if (this.nextTime <= gameTime) {
            RandomSource random = RandomSource.create();
            this.nextTime = gameTime + random.nextIntBetweenInclusive(ModConfig.minCurseDelay, ModConfig.maxCurseDelay);
        } else {
            return;
        }

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            for (ItemStack stack : player.getHandSlots()) {
                stack.getCapability(EnergyCollectorImpl.INSTANCE).ifPresent(cap -> {
                    if (cap.negativeEnergy().test()) {
                        if (Util.tryToApplyCurse(player, stack)) {
                            // summon custom lightning
                            LightningBolt entity = EntityType.LIGHTNING_BOLT.create(player.level);
                            //noinspection ConstantConditions
                            LightningHelper.setColor(entity, 0xFF0000);
                            entity.moveTo(player.position());
                            entity.setVisualOnly(true);
                            player.level.addFreshEntity(entity);
                        }
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (!ModConfig.NegativeEnergy.enabled) {
            return;
        }

        if (event.phase == TickEvent.Phase.END || event.level.isClientSide) {
            return;
        }

        // run this only once a second
        if (event.level.getGameTime() % 20 != 0) {
            return;
        }

        MinecraftServer server = event.level.getServer();
        //noinspection ConstantConditions
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (event.level == player.level && player.level == server.getLevel(Level.NETHER)) {
                for (ItemStack stack : player.getHandSlots()) {
                    stack.getCapability(EnergyCollectorImpl.INSTANCE).ifPresent(cap -> {
                        cap.addEnergy(ModConfig.NegativeEnergy.netherAddition);
                    });
                }
            }
        }
    }

    @SubscribeEvent
    public void onDealDamage(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity causer) {
            RandomSource random = causer.level.random;
            LivingEntity victim = event.getEntity();

            if (ModConfig.NegativeEnergy.enabled && victim.getMobType() == MobType.UNDEAD) {
                causer.getMainHandItem().getCapability(EnergyCollectorImpl.INSTANCE).ifPresent(cap -> {
                    cap.addEnergy(ModConfig.NegativeEnergy.hurtAddition);
                });
            }

            boolean receiveKarma = Util.enchantmentInHand(causer, ModEnchantments.curseOfKarma);
            if (receiveKarma && causer.getRandom().nextDouble() <= ModConfig.karmaChance) {
                causer.hurt(ModDamageSources.KARMA, event.getAmount());
            }

            boolean healDontHurt = Util.enchantmentInHand(causer, ModEnchantments.curseOfKindness);
            if (healDontHurt && causer.getRandom().nextDouble() <= ModConfig.kindnessChance) {
                victim.heal(event.getAmount());
                ((ServerLevel) victim.level).sendParticles(ParticleTypes.HEART, victim.getRandomX(1), victim.getRandomY() + 0.5, victim.getRandomZ(1), 10, 0, 0, 0, random.nextGaussian() * 0.02D);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LightningBolt lightning) {
            BlockPos strikePosition = lightning.getStrikePosition();
            if (lightning.level.getBlockState(strikePosition).is(ModBlocks.maledictusAufero)) {
                lightning.setDamage(0);
                lightning.setVisualOnly(ModConfig.safeLightnings);
                LightningHelper.setCursed(lightning);
                LightningHelper.setColor(lightning, Util.LIGHTNING_COLOR);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = "maledicta", bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {

        @SubscribeEvent
        public static void registerCapability(RegisterCapabilitiesEvent event) {
            event.register(EnergyCollector.class);
        }

        @SubscribeEvent
        public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(EntityType.LIGHTNING_BOLT, ColoredLightningBoltRenderer::new);
        }
    }
}
