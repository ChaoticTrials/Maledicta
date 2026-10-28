package de.melanx.maledicta.data;

import de.melanx.maledicta.Maledicta;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.loot.ChestLootProviderBase;

public class ChestLootProvider extends ChestLootProviderBase {

    public static final ResourceKey<LootTable> FORTRESS_ROD_ROOM = ResourceKey.create(Registries.LOOT_TABLE, Maledicta.getInstance().id("chests/fortress_rod_room"));

    public ChestLootProvider(DatagenContext ctx) {
        super(ctx);
    }

    @Override
    protected void setup() {
        this.customLootTable("fortress_rod_room", LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(0, 2))
                        .add(LootItem.lootTableItem(Items.BOOK)
                                .apply(new EnchantWithLevelsFunction.Builder(UniformGenerator.between(20, 30)))))
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1, 2))
                        .add(this.filler(Items.GOLD_INGOT, 3, 2, 5))
                        .add(this.filler(Items.CRYING_OBSIDIAN, 2, 1, 3))
                        .add(this.filler(Items.EXPERIENCE_BOTTLE, 1, 1, 3))
                        .add(this.filler(Items.NETHERITE_SCRAP, 1, 1, 1))));

        this.customLootTable("cursed_cairn", LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1, 2))
                        .add(LootItem.lootTableItem(Items.BOOK)
                                .apply(new EnchantWithLevelsFunction.Builder(UniformGenerator.between(30, 45)))))
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(2, 4))
                        .add(this.filler(Items.GOLD_INGOT, 3, 2, 5))
                        .add(this.filler(Items.CRYING_OBSIDIAN, 2, 1, 3))
                        .add(this.filler(Items.AMETHYST_SHARD, 2, 3, 6))
                        .add(this.filler(Items.EXPERIENCE_BOTTLE, 1, 2, 4))));
    }

    private LootPoolSingletonContainer.Builder<?> filler(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item)
                .setWeight(weight)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
    }
}
