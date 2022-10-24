package de.melanx.maledicta.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

/**
 * The event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS} whenever an item could get
 * a new curse due to negative energy. The event is cancelable, if canceled, the curse won't be applied. The event
 * does not have a result.
 */
public class ApplyItemCurseEvent extends Event {

    private final Player player;
    private final ItemStack stack;
    
    @Nullable
    private Enchantment enchantment;
    private boolean force;

    public ApplyItemCurseEvent(Player player, ItemStack stack, @Nullable Enchantment enchantment) {
        this.player = player;
        this.stack = stack;
        this.enchantment = enchantment;
        this.force = false;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getStack() {
        return stack;
    }

    /**
     * Gets the enchantment that will be applied.
     */
    @Nullable
    public Enchantment getEnchantment() {
        return enchantment;
    }

    /**
     * Gets whether the enchantment is forced (will be applied even if it does not match the ItemStack)
     */
    public boolean isForced() {
        return force;
    }

    /**
     * Sets the enchantment that will be applied.
     */
    public void setEnchantment(@Nullable Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    /**
     * Sets whether the enchantment is forced (will be applied even if it does not match the ItemStack)
     */
    public void setForced(boolean force) {
        this.force = force;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
