package de.melanx.maledicta.data;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.moddingx.libx.annotation.data.Datagen;
import org.moddingx.libx.datagen.provider.ItemModelProviderBase;
import org.moddingx.libx.mod.ModX;

@Datagen
public class ItemModelProvider extends ItemModelProviderBase {

    public ItemModelProvider(ModX mod, PackOutput packOutput, ExistingFileHelper fileHelper) {
        super(mod, packOutput, fileHelper);
    }

    @Override
    protected void setup() {
        // NO-OP
    }
}
