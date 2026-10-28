package de.melanx.maledicta.data;

import de.melanx.maledicta.Maledicta;
import de.melanx.maledicta.registration.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.tags.CommonTagsProviderBase;

public class ModTagsProvider extends CommonTagsProviderBase {

    public static final TagKey<Item> ALL_WEAPONS = TagKey.create(Registries.ITEM, Maledicta.getInstance().id("all_weapons"));

    public ModTagsProvider(DatagenContext ctx) {
        super(ctx);
    }

    @Override
    public void setup() {
        this.block(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.maledictusAufero);
        this.block(BlockTags.LIGHTNING_RODS)
                .add(ModBlocks.maledictusAufero);
        this.item(ALL_WEAPONS)
                .addTag(ItemTags.WEAPON_ENCHANTABLE)
                .addTag(ItemTags.MINING_LOOT_ENCHANTABLE);
    }
}
