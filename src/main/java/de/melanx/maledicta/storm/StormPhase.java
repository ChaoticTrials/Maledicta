package de.melanx.maledicta.storm;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;
import java.util.Locale;

public enum StormPhase implements StringRepresentable {
    CALM,
    BREWING,
    ACTIVE,
    CLEARING;

    public static final Codec<StormPhase> CODEC = StringRepresentable.fromEnum(StormPhase::values);
    public static final StreamCodec<ByteBuf, StormPhase> STREAM_CODEC = ByteBufCodecs.idMapper(id -> StormPhase.values()[id], StormPhase::ordinal);

    public StormPhase next() {
        return switch (this) {
            case CALM -> BREWING;
            case BREWING -> ACTIVE;
            case ACTIVE -> CLEARING;
            case CLEARING -> CALM;
        };
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
