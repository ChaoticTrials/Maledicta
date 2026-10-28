package de.melanx.maledicta.blocks;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nullable;

public record LeveledEnchantment(Holder<Enchantment> enchantment, int level) {

    private static final MapCodec<Holder<Enchantment>> ENCHANTMENT_FIELD_CODEC = Codec.mapEither(
            Enchantment.CODEC.fieldOf("enchantment"),
            Enchantment.CODEC.fieldOf("curse")
    ).xmap(Either::unwrap, Either::left);

    public static final Codec<LeveledEnchantment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ENCHANTMENT_FIELD_CODEC.forGetter(LeveledEnchantment::enchantment),
            ExtraCodecs.POSITIVE_INT.fieldOf("level").forGetter(LeveledEnchantment::level)
    ).apply(instance, LeveledEnchantment::new));

    public int appliedLevel() {
        return Math.min(this.level, this.enchantment.value().getMaxLevel());
    }

    @Nullable
    public LeveledEnchantment lowered() {
        return this.level > 1 ? new LeveledEnchantment(this.enchantment, this.level - 1) : null;
    }
}
