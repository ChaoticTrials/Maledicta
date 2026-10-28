package de.melanx.maledicta.data;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.sandbox.StructureSetProviderBase;

public class StructureSetProvider extends StructureSetProviderBase {

    public final Holder<StructureSet> cursedCairn = this.structureSet()
            .entry(this.holder(StructureProvider.CURSED_CAIRN))
            .placeRandom(16, 12)
            .frequency(1)
            .build();

    public StructureSetProvider(DatagenContext ctx) {
        super(ctx);
    }
}
