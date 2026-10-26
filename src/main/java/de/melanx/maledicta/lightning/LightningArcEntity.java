package de.melanx.maledicta.lightning;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LightningArcEntity extends Entity {

    public static final int LIFETIME = 10;

    private static final EntityDataAccessor<Vector3fc> OFFSET = SynchedEntityData.defineId(LightningArcEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(LightningArcEntity.class, EntityDataSerializers.INT);

    @Nullable
    private LightningArcGeometry geometry;
    private final long seed;

    public LightningArcEntity(EntityType<? extends LightningArcEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.seed = this.random.nextLong();
    }

    public void setup(Vec3 from, Vec3 to, int color) {
        this.snapTo(from);
        this.setTarget(to);
        this.setColor(color);
    }

    // Has to be called after the start is set, the end is saved relative to it to stay precise far away from the origin
    public void setTarget(Vec3 target) {
        Vec3 offset = target.subtract(this.position());
        this.entityData.set(OFFSET, new Vector3f((float) offset.x, (float) offset.y, (float) offset.z));
    }

    // the vector from the start to the end
    public Vec3 getOffset() {
        Vector3fc offset = this.entityData.get(OFFSET);
        return new Vec3(offset.x(), offset.y(), offset.z());
    }

    public void setColor(int hex) {
        this.entityData.set(COLOR, hex);
    }

    public int getColor() {
        return this.entityData.get(COLOR);
    }

    public long getSeed() {
        return this.seed;
    }

    public LightningArcGeometry getGeometry() {
        if (this.geometry == null) {
            this.geometry = LightningArcGeometry.create(this.getOffset(), this.seed);
        }

        return this.geometry;
    }

    @Override
    public void onSyncedDataUpdated(@Nonnull EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);

        if (OFFSET.equals(accessor)) {
            this.geometry = null;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount > LIFETIME) {
            this.discard();
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 128 * 128;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(OFFSET, new Vector3f());
        entityData.define(COLOR, -1);
    }

    @Override
    public boolean hurtServer(@Nonnull ServerLevel level, @Nonnull DamageSource source, float damage) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull ValueInput input) {
        // only for summoning
        input.read("offset", Vec3.CODEC).ifPresent(offset -> this.entityData.set(OFFSET, new Vector3f((float) offset.x, (float) offset.y, (float) offset.z)));
        this.entityData.set(COLOR, input.getIntOr("color", -1));
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull ValueOutput output) {
        // not saved
    }
}
