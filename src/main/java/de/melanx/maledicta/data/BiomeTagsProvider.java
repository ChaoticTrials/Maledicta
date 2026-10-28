package de.melanx.maledicta.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.tags.TagProviderBase;

public class BiomeTagsProvider extends TagProviderBase<Biome> {

    public BiomeTagsProvider(DatagenContext ctx) {
        super(ctx, Registries.BIOME);
    }

    @Override
    public void setup() {
        this.keyTag(StructureProvider.HAS_CURSED_CAIRN).addTag(Tags.Biomes.IS_MOUNTAIN);
    }

    private TagAppender<ResourceKey<Biome>, Biome> keyTag(TagKey<Biome> tag) {
        TagBuilder builder = this.getOrCreateRawBuilder(tag);

        return TagAppender.forBuilder(builder);
    }
}
