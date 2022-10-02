package de.melanx.maledicta;

import de.melanx.maledicta.registration.ModDamageSources;
import de.melanx.maledicta.registration.ModEnchantments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventListener {

    @SubscribeEvent
    public void onDealDamage(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity causer) {
            RandomSource random = causer.level.random;
            boolean receiveKarma = Util.enchantmentInHand(causer, ModEnchantments.curseOfKarma);
            if (receiveKarma && causer.getRandom().nextDouble() <= ModConfig.karmaChance) {
                causer.hurt(ModDamageSources.KARMA, event.getAmount());
            }

            boolean healDontHurt = Util.enchantmentInHand(causer, ModEnchantments.curseOfKindness);
            if (healDontHurt && causer.getRandom().nextDouble() <= ModConfig.kindnessChance) {
                LivingEntity victim = event.getEntity();
                victim.heal(event.getAmount());
                ((ServerLevel) victim.level).sendParticles(ParticleTypes.HEART, victim.getRandomX(1), victim.getRandomY() + 0.5, victim.getRandomZ(1), 10, 0, 0, 0, random.nextGaussian() * 0.02D);
                event.setCanceled(true);
            }
        }
    }
}
