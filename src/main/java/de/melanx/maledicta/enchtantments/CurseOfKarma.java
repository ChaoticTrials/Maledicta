package de.melanx.maledicta.enchtantments;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class CurseOfKarma extends Enchantment {

    public static final DamageSource DAMAGE_SOURCE = new DamageSource("maledicta_karma");

    public CurseOfKarma() {
        super(Rarity.VERY_RARE, EnchantmentCategory.create("more_weapons", item -> EnchantmentCategory.WEAPON.canEnchant(item)
                        || EnchantmentCategory.DIGGER.canEnchant(item)),
                new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isCurse() {
        return true;
    }
}
