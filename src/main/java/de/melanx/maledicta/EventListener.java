package de.melanx.maledicta;

import de.melanx.maledicta.enchtantments.CurseOfKarma;
import de.melanx.maledicta.registration.ModEnchantments;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventListener {

    @SubscribeEvent
    public void onDealDamage(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity causer) {
            boolean receiveKarma = causer.getItemInHand(InteractionHand.MAIN_HAND).getEnchantmentLevel(ModEnchantments.curseOfKarma) >= 1
                    || causer.getItemInHand(InteractionHand.OFF_HAND).getEnchantmentLevel(ModEnchantments.curseOfKarma) >= 1;
            if (receiveKarma && causer.getRandom().nextDouble() <= ModConfig.karmaChance) {
                causer.hurt(CurseOfKarma.DAMAGE_SOURCE, event.getAmount());
            }
        }
    }
}
