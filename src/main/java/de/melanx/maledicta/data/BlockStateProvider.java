package de.melanx.maledicta.data;

import de.melanx.maledicta.registration.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.moddingx.libx.annotation.data.Datagen;
import org.moddingx.libx.datagen.provider.BlockStateProviderBase;
import org.moddingx.libx.mod.ModX;

@Datagen
public class BlockStateProvider extends BlockStateProviderBase {

    public BlockStateProvider(ModX mod, PackOutput packOutput, ExistingFileHelper fileHelper) {
        super(mod, packOutput, fileHelper);
    }

    @Override
    protected void setup() {
        this.manualModel(ModBlocks.maledictusAufero);
    }
}
