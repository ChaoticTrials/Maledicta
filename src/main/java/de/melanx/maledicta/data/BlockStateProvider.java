package de.melanx.maledicta.data;

import de.melanx.maledicta.registration.ModBlocks;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.model.BlockStateProviderBase;

public class BlockStateProvider extends BlockStateProviderBase {

    public BlockStateProvider(DatagenContext context) {
        super(context);
    }

    @Override
    protected void setup() {
        this.manualModel(ModBlocks.maledictusAufero);
    }
}
