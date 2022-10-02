package de.melanx.maledicta.mixin;

import de.melanx.maledicta.Util;
import de.melanx.maledicta.registration.ModEnchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(
            method = "hurtAndBreak",
            at = @At("RETURN")
    )
    public <T extends LivingEntity> void lol(int amount, T entity, Consumer<T> onBroken, CallbackInfo ci) {
        if (amount > 0) {
            ItemStack stack = (ItemStack) (Object) this;
            if (stack.getEnchantmentLevel(ModEnchantments.curseOfRandomness) >= 1) {
                Util.mixEnchantments(stack);
            }
        }
    }
}
