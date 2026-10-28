package de.melanx.maledicta.data;

import de.melanx.maledicta.Maledicta;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.sandbox.StructureProviderBase;

public class StructureProvider extends StructureProviderBase {

    public static final ResourceKey<Structure> CURSED_CAIRN = ResourceKey.create(Registries.STRUCTURE, Maledicta.getInstance().id("cursed_cairn"));
    public static final TagKey<Biome> HAS_CURSED_CAIRN = TagKey.create(Registries.BIOME, Maledicta.getInstance().id("has_structure/cursed_cairn"));

    public final Holder<Structure> cursedCairn = this.jigsaw(this.holder(TemplatePoolProvider.CURSED_CAIRN))
            .height(Heightmap.Types.WORLD_SURFACE_WG, -3)
            .structure()
            .biomes(HAS_CURSED_CAIRN)
            .terrain(TerrainAdjustment.NONE)
            .build();

    public StructureProvider(DatagenContext ctx) {
        super(ctx);
    }
}
