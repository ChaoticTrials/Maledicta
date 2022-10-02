package de.melanx.maledicta.registration;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.moddingx.libx.annotation.registration.Reg;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "ENCHANTMENT_REGISTRY")
public class ModEnchantments {

    @Reg.Exclude
    public static final EnchantmentCategory ALL_WEAPONS = EnchantmentCategory.create("more_weapons", item -> EnchantmentCategory.WEAPON.canEnchant(item) || EnchantmentCategory.DIGGER.canEnchant(item));
    @Reg.Exclude
    public static final EquipmentSlot[] HAND_SLOTS = new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};

    private static class ModCurse extends Enchantment {

        protected ModCurse(EnchantmentCategory category, EquipmentSlot[] applicableSlots) {
            super(Rarity.VERY_RARE, category, applicableSlots);
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

    public static final Enchantment curseOfKarma = new ModCurse(ALL_WEAPONS, HAND_SLOTS);
    public static final Enchantment curseOfKindness = new ModCurse(ALL_WEAPONS, HAND_SLOTS);
    public static final Enchantment curseOfRandomness = new ModCurse(EnchantmentCategory.BREAKABLE, EquipmentSlot.values());
}
