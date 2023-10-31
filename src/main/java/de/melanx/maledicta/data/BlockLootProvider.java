package de.melanx.maledicta.data;

import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.loot.BlockLootProviderBase;

public class BlockLootProvider extends BlockLootProviderBase {

    public BlockLootProvider(DatagenContext context) {
        super(context);
    }

    @Override
    protected void setup() {
        // NO-OP
    }
}
