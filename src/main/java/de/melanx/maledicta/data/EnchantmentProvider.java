package de.melanx.maledicta.data;

import de.melanx.maledicta.Maledicta;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.EnchantmentProviderBase;

public class EnchantmentProvider extends EnchantmentProviderBase {

    public static final ResourceKey<Enchantment> KARMA = ResourceKey.create(Registries.ENCHANTMENT, Maledicta.getInstance().resource("curse_of_karma"));
    public static final ResourceKey<Enchantment> KINDNESS = ResourceKey.create(Registries.ENCHANTMENT, Maledicta.getInstance().resource("curse_of_kindness"));
    public static final ResourceKey<Enchantment> RANDOMNESS = ResourceKey.create(Registries.ENCHANTMENT, Maledicta.getInstance().resource("curse_of_randomness"));
    public static final ResourceKey<Enchantment> FLEETING_RICHES = ResourceKey.create(Registries.ENCHANTMENT, Maledicta.getInstance().resource("curse_of_fleeting_riches"));

    public final Holder<Enchantment> curseOfKarma = this.enchantment(Component.translatable("enchantment.maledicta.curse_of_karma"))
            .supportedItems(ItemTagsProvider.ALL_WEAPONS)
            .slot(EquipmentSlotGroup.HAND)
            .minCost(25, 0)
            .maxCost(50, 0)
            .build();
    public final Holder<Enchantment> curseOfKindness = this.enchantment(Component.translatable("enchantment.maledicta.curse_of_kindness"))
            .supportedItems(ItemTagsProvider.ALL_WEAPONS)
            .slot(EquipmentSlotGroup.HAND)
            .minCost(25, 0)
            .maxCost(50, 0)
            .build();
    public final Holder<Enchantment> curseOfRandomness = this.enchantment(Component.translatable("enchantment.maledicta.curse_of_randomness"))
            .supportedItems(ItemTags.DURABILITY_ENCHANTABLE)
            .slot(EquipmentSlotGroup.ANY)
            .minCost(25, 0)
            .maxCost(50, 0)
            .build();
    public final Holder<Enchantment> curseOfFleetingRiches = this.enchantment(Component.translatable("enchantment.maledicta.curse_of_fleeting_riches"))
            .supportedItems(ItemTags.MINING_LOOT_ENCHANTABLE)
            .slot(EquipmentSlotGroup.MAINHAND)
            .minCost(25, 0)
            .maxCost(50, 0)
            .build();

    public EnchantmentProvider(DatagenContext ctx) {
        super(ctx);
    }
}
