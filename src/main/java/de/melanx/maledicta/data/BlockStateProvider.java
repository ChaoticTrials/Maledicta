package de.melanx.maledicta.data;

import de.melanx.maledicta.registration.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.moddingx.libx.annotation.data.Datagen;
import org.moddingx.libx.datagen.provider.BlockStateProviderBase;
import org.moddingx.libx.mod.ModX;

@Datagen
public class BlockStateProvider extends BlockStateProviderBase {

    public BlockStateProvider(ModX mod, DataGenerator generator, ExistingFileHelper fileHelper) {
        super(mod, generator, fileHelper);
    }

    @Override
    protected void setup() {
        this.manualModel(ModBlocks.maledictusAufero);
        this.manualModel(ModBlocks.purificationCrystal);
    }
}
