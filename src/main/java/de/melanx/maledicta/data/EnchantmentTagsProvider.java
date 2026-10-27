package de.melanx.maledicta.data;

import de.melanx.maledicta.Maledicta;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.tags.TagProviderBase;

import java.util.List;

public class EnchantmentTagsProvider extends TagProviderBase<Enchantment> {

    public static final TagKey<Enchantment> PREFILLED_ROD_ENCHANTMENTS = TagKey.create(Registries.ENCHANTMENT, Maledicta.getInstance().id("prefilled_rod_enchantments"));

    public EnchantmentTagsProvider(DatagenContext ctx) {
        super(ctx, Registries.ENCHANTMENT);
    }

    @Override
    public void setup() {
        List<ResourceKey<Enchantment>> enchantmentKeys = List.of(
                EnchantmentProvider.KARMA,
                EnchantmentProvider.KINDNESS,
                EnchantmentProvider.RANDOMNESS,
                EnchantmentProvider.FLEETING_RICHES,
                EnchantmentProvider.DOOM,
                EnchantmentProvider.STRIFE
        );

        this.keyTag(EnchantmentTags.TREASURE).addAll(enchantmentKeys);
        this.keyTag(EnchantmentTags.CURSE).addAll(enchantmentKeys);
        this.keyTag(EnchantmentTags.ON_RANDOM_LOOT).addAll(enchantmentKeys);
        this.keyTag(EnchantmentTags.TRADEABLE).addAll(enchantmentKeys);

        this.keyTag(PREFILLED_ROD_ENCHANTMENTS).addAll(enchantmentKeys);
    }

    private TagAppender<ResourceKey<Enchantment>, Enchantment> keyTag(TagKey<Enchantment> tag) {
        TagBuilder builder = this.getOrCreateRawBuilder(tag);

        return TagAppender.forBuilder(builder);
    }
}
