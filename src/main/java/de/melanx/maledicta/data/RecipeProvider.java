package de.melanx.maledicta.data;

import de.melanx.maledicta.registration.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.Tags;
import org.moddingx.libx.annotation.data.Datagen;
import org.moddingx.libx.datagen.provider.recipe.RecipeProviderBase;
import org.moddingx.libx.datagen.provider.recipe.crafting.CraftingExtension;
import org.moddingx.libx.mod.ModX;

@Datagen
public class RecipeProvider extends RecipeProviderBase implements CraftingExtension {

    public RecipeProvider(ModX mod, PackOutput packOutput) {
        super(mod, packOutput);
    }

    @Override
    protected void setup() {
        this.shaped(ModBlocks.maledictusAufero, "B", "O", "O", 'B', Tags.Items.INGOTS_NETHERITE, 'O', Tags.Items.INGOTS_NETHERITE);
    }
}
