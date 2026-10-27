package de.melanx.maledicta.storm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.maledicta.Maledicta;
import de.melanx.maledicta.ModConfig;
import de.melanx.maledicta.network.ModNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import javax.annotation.Nullable;

public class CurseStorm extends SavedData {

    public static final Codec<CurseStorm> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StormPhase.CODEC.fieldOf("phase").forGetter(storm -> storm.phase),
            Codec.INT.fieldOf("ticks_left").forGetter(storm -> storm.ticksLeft),
            Codec.INT.fieldOf("phase_length").forGetter(storm -> storm.phaseLength)
    ).apply(instance, CurseStorm::new));

    public static final SavedDataType<CurseStorm> TYPE = new SavedDataType<>(Maledicta.getInstance().id("curse_storm"), CurseStorm::new, CODEC, null);

    // the client gets the remaining time sometimes, so its countdown does not drift away
    private static final int RESYNC_INTERVAL = 600;

    private StormPhase phase;
    // -1 until the first calm got its random length
    private int ticksLeft;
    private int phaseLength;

    private CurseStorm() {
        this(StormPhase.CALM, -1, -1);
    }

    private CurseStorm(StormPhase phase, int ticksLeft, int phaseLength) {
        this.phase = phase;
        this.ticksLeft = ticksLeft;
        this.phaseLength = phaseLength;
    }

    public static boolean hasStorms(Level level) {
        return level.dimension() == Level.NETHER;
    }

    @Nullable
    public static CurseStorm get(ServerLevel level) {
        return CurseStorm.hasStorms(level) ? level.getDataStorage().computeIfAbsent(TYPE) : null;
    }

    public static boolean isActive(ServerLevel level) {
        if (!ModConfig.CurseStorm.enabled) {
            return false;
        }

        CurseStorm storm = CurseStorm.get(level);
        return storm != null && storm.phase == StormPhase.ACTIVE;
    }

    public StormPhase phase() {
        return this.phase;
    }

    public int ticksLeft() {
        return this.ticksLeft;
    }

    public void tick(ServerLevel level) {
        if (!ModConfig.CurseStorm.enabled) {
            if (this.phase != StormPhase.CALM) {
                this.setPhase(level, StormPhase.CALM);
            }

            return;
        }

        if (this.ticksLeft < 0) {
            this.setPhase(level, StormPhase.CALM);
            return;
        }

        this.ticksLeft--;
        this.setDirty();
        if (this.ticksLeft <= 0) {
            this.setPhase(level, this.phase.next());
        } else if (this.ticksLeft % RESYNC_INTERVAL == 0) {
            this.sync(level);
        }
    }

    public void setPhase(ServerLevel level, StormPhase phase) {
        this.phase = phase;
        this.phaseLength = CurseStorm.rollLength(level.getRandom(), phase);
        this.ticksLeft = this.phaseLength;
        this.setDirty();
        this.sync(level);
    }

    public void sync(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            this.sync(player);
        }
    }

    public void sync(ServerPlayer player) {
        ModNetwork.curseStorm(player, true, this.phase, Math.max(this.ticksLeft, 0), Math.max(this.phaseLength, 1));
    }

    // Tells the player about the storm of the dimension they are in, or that there is none
    public static void syncTo(ServerPlayer player) {
        CurseStorm storm = player.level() instanceof ServerLevel level ? CurseStorm.get(level) : null;
        if (storm != null && ModConfig.CurseStorm.enabled) {
            storm.sync(player);
        } else {
            ModNetwork.curseStorm(player, false, StormPhase.CALM, 0, 1);
        }
    }

    private static int rollLength(RandomSource random, StormPhase phase) {
        return switch (phase) {
            case CALM -> CurseStorm.between(random, ModConfig.CurseStorm.Interval.min, ModConfig.CurseStorm.Interval.max);
            case BREWING -> CurseStorm.between(random, ModConfig.CurseStorm.Brewing.min, ModConfig.CurseStorm.Brewing.max);
            case ACTIVE -> CurseStorm.between(random, ModConfig.CurseStorm.Duration.min, ModConfig.CurseStorm.Duration.max);
            case CLEARING -> ModConfig.CurseStorm.clearing;
        };
    }

    // min and max may be swapped in the config
    private static int between(RandomSource random, int a, int b) {
        return Mth.randomBetweenInclusive(random, Math.min(a, b), Math.max(a, b));
    }
}
