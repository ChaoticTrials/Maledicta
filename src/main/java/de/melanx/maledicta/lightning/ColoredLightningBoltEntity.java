package de.melanx.maledicta.lightning;

import de.melanx.maledicta.ModConfig;
import de.melanx.maledicta.blocks.MaledictusAufero;
import de.melanx.maledicta.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ColoredLightningBoltEntity extends LightningBolt {

    public static final EntityDataAccessor<Integer> COLOR_ACCESSOR = SynchedEntityData.defineId(ColoredLightningBoltEntity.class, EntityDataSerializers.INT);

    public ColoredLightningBoltEntity(EntityType<? extends LightningBolt> entityType, Level level) {
        super(entityType, level);
    }

    public void setColor(int hex) {
        this.entityData.set(COLOR_ACCESSOR, hex);
    }

    public int getColor() {
        return this.entityData.get(COLOR_ACCESSOR);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ColoredLightningBoltEntity.COLOR_ACCESSOR, -1);
    }

    @Override
    protected void powerLightningRod() {
        BlockPos strikePosition = this.getStrikePosition();
        BlockState blockState = this.level().getBlockState(strikePosition);

        if (blockState.is(ModBlocks.maledictusAufero)) {
            this.setVisualOnly(ModConfig.safeLightnings);
            ((MaledictusAufero) blockState.getBlock()).onLightningStrike(blockState, this.level(), strikePosition);
        }
    }
}
