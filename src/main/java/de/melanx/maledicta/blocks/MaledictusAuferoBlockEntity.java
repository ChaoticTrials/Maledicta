package de.melanx.maledicta.blocks;

import com.mojang.serialization.Codec;
import de.melanx.maledicta.ModConfig;
import de.melanx.maledicta.registration.ModBlockEntities;
import de.melanx.maledicta.util.AuferoEffects;
import de.melanx.maledicta.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MaledictusAuferoBlockEntity extends BlockEntity {

    private static final Codec<List<StoredCurse>> CURSES_CODEC = StoredCurse.CODEC.listOf();

    // the oldest curse comes first
    private final List<StoredCurse> curses = new ArrayList<>();
    private float decayProgress;
    private boolean canDecay;
    private float jumpProgress;

    public MaledictusAuferoBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.maledictusAufero, pos, state);
    }

    public boolean isFull() {
        return this.curses.size() >= ModConfig.MaledictusAufero.curseCapacity;
    }

    public boolean store(ServerLevel level, Holder<Enchantment> curse, int curseLevel) {
        if (this.isFull() && !this.makeRoom(level)) {
            return false;
        }

        this.curses.add(new StoredCurse(curse, curseLevel));
        this.setChanged();

        return true;
    }

    private boolean makeRoom(ServerLevel level) {
        if (!ModConfig.MaledictusAufero.overflow || this.curses.isEmpty()) {
            return false;
        }

        if (AuferoEffects.jumpToNearest(level, this.worldPosition, this.curses.getFirst(), Util.LIGHTNING_COLOR)) {
            this.curses.removeFirst();
            return true;
        }

        return false;
    }

    public void serverTick(ServerLevel level, BlockPos pos) {
        if (this.curses.isEmpty()) {
            this.decayProgress = 0;
            return;
        }

        int radius = 4;
        BlockPos start = pos.offset(-radius, -radius, -radius);
        Vec3 rodHead = Vec3.upFromBottomCenterOf(pos, 1);

        for (int i = 0; i < 4; i++) {
            BlockPos randomPos = new BlockPos(start.getX() + level.getRandom().nextInt(radius * 2), pos.getY() + level.getRandom().nextInt(radius), start.getZ() + level.getRandom().nextInt(radius * 2));
            if (MaledictusAuferoBlockEntity.canSee(level, rodHead, Vec3.atCenterOf(randomPos))) {
                AuferoEffects.arc(level, rodHead, Vec3.atCenterOf(randomPos), Util.LIGHTNING_COLOR, false);
            }
        }

        // bad energy makes jumps and decay happen more often, so the progress is measured in fractions of the interval
        double bad = this.badEnergy(level);
        this.jumpProgress += (float) ((1 + bad * ModConfig.MaledictusAufero.Jump.boost) / ModConfig.MaledictusAufero.Jump.interval);
        if (this.jumpProgress >= 1) {
            this.jumpProgress = 0;
            this.jumpStep(level, pos);
            if (this.curses.isEmpty()) {
                this.decayProgress = 0;
                return;
            }
        }

        if (!this.canDecay) {
            return;
        }

        this.decayProgress += (float) ((1 + bad * ModConfig.MaledictusAufero.Decay.boost) / ModConfig.MaledictusAufero.Decay.interval);
        if (this.decayProgress >= 1) {
            this.decayProgress = 0;
            this.decayStep(level, pos);
            this.setChanged();
        }
    }

    // Whether no block is in the way between the two. The target itself may be solid, the arc then ends on its surface
    private static boolean canSee(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 offset = to.subtract(from);
        if (offset.length() < 1) {
            return false;
        }

        if (level.getBlockState(BlockPos.containing(to)).isAir()) {
            return false;
        }

        // start outside the block the ray comes from, otherwise it would hit that block right away
        Vec3 start = from.add(offset.normalize().scale(0.9));
        BlockHitResult hit = level.clip(new ClipContext(start, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));

        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(BlockPos.containing(to));
    }

    // Gives one random player in range the chance to get one random curse. Happens often, so a player who walks by is in danger
    private void jumpStep(ServerLevel level, BlockPos pos) {
        List<ServerPlayer> players = AuferoEffects.playersNear(level, pos.getCenter(), ModConfig.MaledictusAufero.Jump.range);
        if (players.isEmpty()) {
            return;
        }

        ServerPlayer player = players.get(level.getRandom().nextInt(players.size()));
        int index = level.getRandom().nextInt(this.curses.size());
        if (this.tryJump(level, pos, player, this.curses.get(index), ModConfig.MaledictusAufero.Jump.chance)) {
            this.curses.remove(index);
            this.setChanged();
        }
    }

    // A random curse tries to jump to the players in range, one after another. If it does not jump, it loses a level.
    private void decayStep(ServerLevel level, BlockPos pos) {
        int index = level.getRandom().nextInt(this.curses.size());
        StoredCurse curse = this.curses.get(index);

        List<ServerPlayer> players = AuferoEffects.playersNear(level, pos.getCenter(), ModConfig.MaledictusAufero.Jump.range);
        Collections.shuffle(players);
        for (ServerPlayer player : players) {
            if (this.tryJump(level, pos, player, curse, ModConfig.MaledictusAufero.Decay.jumpChance)) {
                this.curses.remove(index);
                return;
            }
        }

        StoredCurse lowered = curse.lowered();
        if (lowered == null) {
            this.curses.remove(index);
        } else {
            this.curses.set(index, lowered);
        }
    }

    // The closer the player, the likelier. Only counts if the player has an item which can take the curse
    private boolean tryJump(ServerLevel level, BlockPos pos, ServerPlayer player, StoredCurse curse, double baseChance) {
        double distance = Math.sqrt(player.distanceToSqr(pos.getCenter()));
        double chance = baseChance * (1 - distance / ModConfig.MaledictusAufero.Jump.range);

        return level.getRandom().nextDouble() < chance && AuferoEffects.jump(level, pos, player, curse, Util.LIGHTNING_COLOR);
    }

    private double badEnergy(Level level) {
        double bad = 0;
        if (level.isThundering()) {
            bad += ModConfig.MaledictusAufero.BadEnergy.storm;
        } else if (level.isRaining()) {
            bad += ModConfig.MaledictusAufero.BadEnergy.storm / 2;
        }

        if (level.isDarkOutside()) {
            bad += ModConfig.MaledictusAufero.BadEnergy.night;
        }

        if (level.dimension() != Level.OVERWORLD) {
            bad += ModConfig.MaledictusAufero.BadEnergy.dimension;
        }

        bad += ModConfig.MaledictusAufero.BadEnergy.fill * Math.min(1, (double) this.curses.size() / ModConfig.MaledictusAufero.curseCapacity);

        return bad;
    }

    @Override
    public void preRemoveSideEffects(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super.preRemoveSideEffects(pos, state);

        // only called when the block really goes away, not when its state changes
        if (this.level instanceof ServerLevel serverLevel && !this.curses.isEmpty()) {
            List<StoredCurse> released = List.copyOf(this.curses);
            this.curses.clear();
            AuferoEffects.breakRod(serverLevel, pos, released);
        }
    }

    @Override
    protected void saveAdditional(@Nonnull ValueOutput output) {
        super.saveAdditional(output);

        output.store("curses", CURSES_CODEC, List.copyOf(this.curses));
        output.putFloat("decay_progress", this.decayProgress);
        output.putBoolean("can_decay", this.canDecay);
    }

    @Override
    protected void loadAdditional(@Nonnull ValueInput input) {
        super.loadAdditional(input);

        this.curses.clear();
        input.read("curses", CURSES_CODEC).ifPresent(this.curses::addAll);
        this.decayProgress = input.getFloatOr("decay_progress", 0F);
        this.canDecay = input.getBooleanOr("can_decay", true);
    }
}
