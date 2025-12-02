package de.melanx.maledicta.data;

import de.melanx.maledicta.Maledicta;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.tags.CommonTagsProviderBase;

public class ItemTagsProvider extends CommonTagsProviderBase {

    public static final TagKey<Item> ALL_WEAPONS = TagKey.create(Registries.ITEM, Maledicta.getInstance().resource("all_weapons"));

    public ItemTagsProvider(DatagenContext ctx) {
        super(ctx);
    }

    @Override
    public void setup() {
        this.item(ALL_WEAPONS)
                .addTag(ItemTags.WEAPON_ENCHANTABLE)
                .addTag(ItemTags.MINING_LOOT_ENCHANTABLE);
    }
}
