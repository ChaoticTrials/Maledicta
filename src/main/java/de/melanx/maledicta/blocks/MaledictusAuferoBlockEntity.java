package de.melanx.maledicta.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.maledicta.ModConfig;
import de.melanx.maledicta.client.ClientArcs;
import de.melanx.maledicta.lightning.ColoredLightningBoltEntity;
import de.melanx.maledicta.registration.ModBlockEntities;
import de.melanx.maledicta.registration.ModEntities;
import de.melanx.maledicta.storm.CurseStorm;
import de.melanx.maledicta.util.AuferoEffects;
import de.melanx.maledicta.util.Util;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

    private static final Codec<List<LeveledEnchantment>> CURSES_CODEC = LeveledEnchantment.CODEC.listOf();
    private static final int[] STORM_COLORS = {0xFF1A1A, 0xC80F0F, 0xFF4D4D, 0x8B0000, 0xE0301E};

    // the oldest enchantment comes first
    private final List<LeveledEnchantment> curses = new ArrayList<>();
    private float decayProgress;
    private boolean canDecay = true;
    private float jumpProgress;
    private boolean cursed;

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

        this.curses.add(new LeveledEnchantment(curse, curseLevel));
        this.setChanged();
        this.syncCursed();

        return true;
    }

    public void prefill(ServerLevel level, PrefillEnchantments prefill) {
        if (level == null) {
            return;
        }

        Registry<Enchantment> enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        HolderSet.Named<Enchantment> possibleEnchantments = enchantments.get(prefill.pool()).orElseThrow();

        int amount = level.getRandom().nextIntBetweenInclusive(prefill.min(), prefill.max());
        for (int i = 0; i < amount; i++) {
            Holder<Enchantment> curse = possibleEnchantments.get(level.getRandom().nextInt(possibleEnchantments.size()));
            this.curses.add(new LeveledEnchantment(curse, curse.value().getMaxLevel()));
        }

        this.cursed = !this.curses.isEmpty();
        this.setChanged();
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
        if (level.getRandom().nextInt(ModConfig.CurseStorm.strikeInterval) == 0 && CurseStorm.isActive(level)) {
            this.stormStrike(level, pos);
        }

        if (this.curses.isEmpty()) {
            this.decayProgress = 0;
            return;
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
            this.syncCursed();
        }
    }

    private void stormStrike(ServerLevel level, BlockPos pos) {
        BlockState state = this.getBlockState();
        if (!(state.getBlock() instanceof MaledictusAufero rod)) {
            return;
        }

        int color = STORM_COLORS[level.getRandom().nextInt(STORM_COLORS.length)];
        ColoredLightningBoltEntity lightning = ModEntities.lightningBolt.create(level, EntitySpawnReason.EVENT);
        if (lightning != null) {
            lightning.setVisualOnly(true);
            lightning.setPowersRod(false);
            lightning.snapTo(Vec3.upFromBottomCenterOf(pos, 1));
            lightning.setColor(color);
            level.addFreshEntity(lightning);
        }

        rod.strike(state, level, pos);
    }

    public void clientTick(Level level, BlockPos pos) {
        Vec3 rodHead = Vec3.upFromBottomCenterOf(pos, 1);
        if (!this.cursed || !ClientArcs.isNear(rodHead)) {
            return;
        }

        int radius = 4;
        BlockPos start = pos.offset(-radius, -radius, -radius);
        for (int i = 0; i < 4; i++) {
            BlockPos randomPos = new BlockPos(start.getX() + level.getRandom().nextInt(radius * 2), pos.getY() + level.getRandom().nextInt(radius), start.getZ() + level.getRandom().nextInt(radius * 2));
            if (MaledictusAuferoBlockEntity.canSee(level, rodHead, Vec3.atCenterOf(randomPos))) {
                ClientArcs.spawn(level, rodHead, Vec3.atCenterOf(randomPos), Util.LIGHTNING_COLOR);
            }
        }
    }

    // Whether no block is in the way between the two. The target itself may be solid, the arc then ends on its surface
    private static boolean canSee(Level level, Vec3 from, Vec3 to) {
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
            this.syncCursed();
        }
    }

    public void setCanDecay(boolean canDecay) {
        this.canDecay = canDecay;
    }

    // A random curse tries to jump to the players in range, one after another. If it does not jump, it loses a level.
    private void decayStep(ServerLevel level, BlockPos pos) {
        int index = level.getRandom().nextInt(this.curses.size());
        LeveledEnchantment curse = this.curses.get(index);

        List<ServerPlayer> players = AuferoEffects.playersNear(level, pos.getCenter(), ModConfig.MaledictusAufero.Jump.range);
        Collections.shuffle(players);
        for (ServerPlayer player : players) {
            if (this.tryJump(level, pos, player, curse, ModConfig.MaledictusAufero.Decay.jumpChance)) {
                this.curses.remove(index);
                return;
            }
        }

        LeveledEnchantment lowered = curse.lowered();
        if (lowered == null) {
            this.curses.remove(index);
        } else {
            this.curses.set(index, lowered);
        }
    }

    // The closer the player, the likelier. Only counts if the player has an item which can take the curse
    private boolean tryJump(ServerLevel level, BlockPos pos, ServerPlayer player, LeveledEnchantment curse, double baseChance) {
        double distance = Math.sqrt(player.distanceToSqr(pos.getCenter()));
        double chance = baseChance * (1 - distance / ModConfig.MaledictusAufero.Jump.range);

        return level.getRandom().nextDouble() < chance && AuferoEffects.jump(level, pos, player, curse, Util.LIGHTNING_COLOR);
    }

    private void syncCursed() {
        boolean hasCurses = !this.curses.isEmpty();
        if (this.cursed != hasCurses) {
            this.cursed = hasCurses;
            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
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

        if (level instanceof ServerLevel serverLevel && CurseStorm.isActive(serverLevel)) {
            bad += ModConfig.MaledictusAufero.BadEnergy.curseStorm;
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
            List<LeveledEnchantment> released = List.copyOf(this.curses);
            this.curses.clear();
            AuferoEffects.breakRod(serverLevel, pos, released);
        }
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag(@Nonnull HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("has_curses", this.cursed);

        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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

        // client only, not saved
        this.cursed = input.getBooleanOr("has_curses", !this.curses.isEmpty());
    }

    public record PrefillEnchantments(TagKey<Enchantment> pool, int min, int max) {

        public static final Codec<PrefillEnchantments> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TagKey.hashedCodec(Registries.ENCHANTMENT).fieldOf("pool").forGetter(PrefillEnchantments::pool),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("min", 4).forGetter(PrefillEnchantments::min),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("max", 8).forGetter(PrefillEnchantments::max)
        ).apply(instance, PrefillEnchantments::new));

        public PrefillEnchantments(TagKey<Enchantment> pool) {
            this(pool, 4, 8);
        }
    }
}
