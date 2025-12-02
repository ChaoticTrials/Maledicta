package de.melanx.maledicta.api;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import javax.annotation.Nullable;

/**
 * The event is fired on the {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS} whenever an item could get
 * a new curse due to negative energy. The event is cancelable, if canceled, the curse won't be applied. The event
 * does not have a result.
 */
public class ApplyItemCurseEvent extends Event implements ICancellableEvent {

    private final Player player;
    private final ItemStack stack;
    
    @Nullable
    private Holder<Enchantment> enchantment;
    private boolean force;

    public ApplyItemCurseEvent(Player player, ItemStack stack, @Nullable Holder<Enchantment> enchantment) {
        this.player = player;
        this.stack = stack;
        this.enchantment = enchantment;
        this.force = false;
    }

    public Player getPlayer() {
        return this.player;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    /**
     * Gets the enchantment that will be applied.
     */
    @Nullable
    public Holder<Enchantment> getEnchantment() {
        return this.enchantment;
    }

    /**
     * Gets whether the enchantment is forced (will be applied even if it does not match the ItemStack)
     */
    public boolean isForced() {
        return this.force;
    }

    /**
     * Sets the enchantment that will be applied.
     */
    public void setEnchantment(@Nullable Holder<Enchantment> enchantment) {
        this.enchantment = enchantment;
    }

    /**
     * Sets whether the enchantment is forced (will be applied even if it does not match the ItemStack)
     */
    public void setForced(boolean force) {
        this.force = force;
    }
}
