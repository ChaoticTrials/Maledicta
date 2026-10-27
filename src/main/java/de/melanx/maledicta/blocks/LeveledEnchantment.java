package de.melanx.maledicta.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nullable;

public record LeveledEnchantment(Holder<Enchantment> enchantment, int level) {

    public static final Codec<LeveledEnchantment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Enchantment.CODEC.fieldOf("enchantment").forGetter(LeveledEnchantment::enchantment),
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
