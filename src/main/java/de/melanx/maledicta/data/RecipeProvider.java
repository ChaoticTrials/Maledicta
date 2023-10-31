package de.melanx.maledicta.data;

import de.melanx.maledicta.registration.ModBlocks;
import net.minecraftforge.common.Tags;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.recipe.RecipeProviderBase;
import org.moddingx.libx.datagen.provider.recipe.crafting.CraftingExtension;

public class RecipeProvider extends RecipeProviderBase implements CraftingExtension {

    public RecipeProvider(DatagenContext context) {
        super(context);
    }

    @Override
    protected void setup() {
        this.shaped(ModBlocks.maledictusAufero, "B", "O", "O", 'B', Tags.Items.INGOTS_NETHERITE, 'O', Tags.Items.INGOTS_NETHERITE);
    }
}
