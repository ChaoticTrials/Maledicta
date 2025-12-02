package de.melanx.maledicta.mixin;

import de.melanx.maledicta.data.EnchantmentProvider;
import de.melanx.maledicta.util.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At("RETURN")
    )
    public void hurtAndBreak(int amount, ServerLevel level, LivingEntity entity, Consumer<Item> onBroken, CallbackInfo ci) {
        if (amount > 0) {
            ItemStack stack = (ItemStack) (Object) this;
            if (stack.getEnchantmentLevel(entity.level().registryAccess().holderOrThrow(EnchantmentProvider.RANDOMNESS)) >= 1) {
                Util.mixEnchantments(stack, entity.registryAccess());
            }
        }
    }
}
