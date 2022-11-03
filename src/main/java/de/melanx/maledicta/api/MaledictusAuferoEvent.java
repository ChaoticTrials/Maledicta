package de.melanx.maledicta.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * The event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS} whenever a lightning bolt
 * hits a maledictus aufero. The event is cancelable, if canceled the maledictus aufero won't have any effects on
 * the nearby items. The event does not have a result.
 */
public class MaledictusAuferoEvent extends Event {
    
    private final Level level;
    private final BlockState state;
    private final BlockPos pos;
    private final List<ItemEntity> allItems;
    private final List<ItemEntity> cursedItems;

    public MaledictusAuferoEvent(Level level, BlockState state, BlockPos pos, List<ItemEntity> allItems, List<ItemEntity> cursedItems) {
        this.level = level;
        this.state = state;
        this.pos = pos.immutable();
        this.allItems = List.copyOf(allItems);
        this.cursedItems = List.copyOf(cursedItems);
    }
    
    public Level getLevel() {
        return this.level;
    }

    public BlockState getState() {
        return this.state;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    /**
     * Gets all items in range of the maledictus aufero. These will get their negative energy halved
     * and may be enchanted with curses from other items if applicable.
     */
    public List<ItemEntity> getAllItems() {
        return this.allItems;
    }

    /**
     * Gets all the cursed items. These will get a curse removed (that may be put onto a different item if applicable).
     */
    public List<ItemEntity> getCursedItems() {
        return this.cursedItems;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
