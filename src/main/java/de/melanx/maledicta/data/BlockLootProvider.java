package de.melanx.maledicta.data;

import net.minecraft.data.PackOutput;
import org.moddingx.libx.annotation.data.Datagen;
import org.moddingx.libx.datagen.provider.loot.BlockLootProviderBase;
import org.moddingx.libx.mod.ModX;

@Datagen
public class BlockLootProvider extends BlockLootProviderBase {

    public BlockLootProvider(ModX mod, PackOutput packOutput) {
        super(mod, packOutput);
    }

    @Override
    protected void setup() {
        // NO-OP
    }
}
