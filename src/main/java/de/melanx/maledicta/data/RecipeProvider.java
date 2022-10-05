package de.melanx.maledicta.data;

import de.melanx.maledicta.registration.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import org.moddingx.libx.annotation.data.Datagen;
import org.moddingx.libx.datagen.provider.recipe.RecipeProviderBase;
import org.moddingx.libx.datagen.provider.recipe.crafting.CraftingExtension;
import org.moddingx.libx.mod.ModX;

@Datagen
public class RecipeProvider extends RecipeProviderBase implements CraftingExtension {

    public RecipeProvider(ModX mod, DataGenerator generator) {
        super(mod, generator);
    }

    @Override
    protected void setup() {
        this.shaped(ModBlocks.maledictusAufero, "B", "O", "O", 'B', Tags.Items.INGOTS_NETHERITE, 'O', Tags.Items.INGOTS_NETHERITE);
        this.shaped(ModBlocks.purificationCrystal, "A A", "AAA", "CLC", 'A', Tags.Items.GEMS_AMETHYST, 'C', Items.BLACK_CANDLE, 'L', ItemTags.LOGS_THAT_BURN);
    }
}
