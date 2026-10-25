package de.melanx.maledicta.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nullable;

public record StoredCurse(Holder<Enchantment> curse, int level) {

    public static final Codec<StoredCurse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Enchantment.CODEC.fieldOf("curse").forGetter(StoredCurse::curse),
            ExtraCodecs.POSITIVE_INT.fieldOf("level").forGetter(StoredCurse::level)
    ).apply(instance, StoredCurse::new));

    public int appliedLevel() {
        return Math.min(this.level, this.curse.value().getMaxLevel());
    }

    @Nullable
    public StoredCurse lowered() {
        return this.level > 1 ? new StoredCurse(this.curse, this.level - 1) : null;
    }
}
