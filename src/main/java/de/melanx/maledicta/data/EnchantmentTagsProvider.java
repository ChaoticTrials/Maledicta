package de.melanx.maledicta.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.tags.TagProviderBase;

import java.util.List;

public class EnchantmentTagsProvider extends TagProviderBase<Enchantment> {

    public EnchantmentTagsProvider(DatagenContext ctx) {
        super(ctx, Registries.ENCHANTMENT);
    }

    @Override
    public void setup() {
        List<ResourceKey<Enchantment>> enchantmentKeys = List.of(
                EnchantmentProvider.KARMA,
                EnchantmentProvider.KINDNESS,
                EnchantmentProvider.RANDOMNESS,
                EnchantmentProvider.FLEETING_RICHES
        );

        this.tag(EnchantmentTags.TREASURE).addAll(enchantmentKeys);
        this.tag(EnchantmentTags.CURSE).addAll(enchantmentKeys);
        this.tag(EnchantmentTags.ON_RANDOM_LOOT).addAll(enchantmentKeys);
        this.tag(EnchantmentTags.TRADEABLE).addAll(enchantmentKeys);
    }
}
