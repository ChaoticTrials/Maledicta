package de.melanx.maledicta.util;

import de.melanx.maledicta.Maledicta;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class Util {

    public static final String LIGHTNING_COLOR = "6905F5";

    public static void unenchant(ItemStack stack, Enchantment enchantment) {
        if (!stack.getOrCreateTag().contains("Enchantments", Tag.TAG_LIST)) {
            return;
        }

        ListTag list = stack.getOrCreateTag().getList("Enchantments", Tag.TAG_COMPOUND);
        for (Tag tag : list) {
            String id = ((CompoundTag) tag).getString("id");
            //noinspection ConstantConditions,deprecation
            if (id.equals(Registry.ENCHANTMENT.getKey(enchantment).toString())) {
                list.remove(tag);
                break;
            }
        }
    }

    // ItemStack#isEnchantable ignoring existing enchantments
    public static boolean isEnchantable(ItemStack stack) {
        return stack.getMaxStackSize() == 1 && stack.isDamageableItem();
    }

    public static boolean tryToApplyCurse(ItemStack stack) {
        List<Enchantment> possibleEnchantments = new ArrayList<>(ForgeRegistries.ENCHANTMENTS.getValues()).stream().filter(enchantment -> enchantment.isCurse() && enchantment.canEnchant(stack)).toList();
        if (possibleEnchantments.isEmpty()) {
            return false;
        }

        RandomSource random = RandomSource.create();
        Enchantment enchantment = possibleEnchantments.get(random.nextInt(possibleEnchantments.size()));
        if (enchantment.canEnchant(stack) && stack.getEnchantmentLevel(enchantment) <= 0) {
            stack.enchant(enchantment, enchantment.getMaxLevel());
            return true;
        }

        return false;
    }

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
        if (futureEnchantments.size() != allEnchantments.size()) {
            Maledicta.getInstance().logger.warn("It seems like enchantments were deleted on {}, previous enchantments: {}", stack, allEnchantments);
        }
    }

    public static boolean enchantmentInHand(LivingEntity entity, Enchantment enchantment) {
        return entity.getItemInHand(InteractionHand.MAIN_HAND).getEnchantmentLevel(enchantment) >= 1
                || entity.getItemInHand(InteractionHand.OFF_HAND).getEnchantmentLevel(enchantment) >= 1;
    }
}
