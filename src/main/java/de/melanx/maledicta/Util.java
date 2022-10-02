package de.melanx.maledicta;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class Util {

    public static void mixEnchantments(ItemStack stack) {
        RandomSource random = RandomSource.create();
        List<Map.Entry<Enchantment, Integer>> allEnchantments = new ArrayList<>(stack.getAllEnchantments().entrySet());
        Map<Enchantment, Integer> futureEnchantments = new HashMap<>();

        List<Enchantment> possibleEnchantments = new ArrayList<>(ForgeRegistries.ENCHANTMENTS.getValues());

        // make sure to handle curses first
        allEnchantments = allEnchantments.stream().sorted(Comparator.comparing(entry -> !entry.getKey().isCurse())).toList();

        allEnchantments.forEach(entry -> {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();
            if (enchantment.isCurse()) {
                futureEnchantments.put(enchantment, level);
                possibleEnchantments.remove(enchantment);
                return;
            }

            while (true) {
                if (possibleEnchantments.isEmpty()) {
                    futureEnchantments.put(enchantment, level);
                    break;
                }

                Enchantment potentialEnchantment = possibleEnchantments.get(random.nextInt(possibleEnchantments.size()));
                possibleEnchantments.remove(potentialEnchantment);
                if (potentialEnchantment.canEnchant(stack) && !futureEnchantments.containsKey(potentialEnchantment)) {
                    futureEnchantments.put(potentialEnchantment, potentialEnchantment.isCurse() ? 1 : level);
                    break;
                }
            }
        });
        EnchantmentHelper.setEnchantments(futureEnchantments, stack);
    }
}
