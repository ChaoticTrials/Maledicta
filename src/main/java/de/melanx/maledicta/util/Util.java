package de.melanx.maledicta.util;

import de.melanx.maledicta.Maledicta;
import de.melanx.maledicta.api.ApplyItemCurseEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

public class Util {

    public static final int LIGHTNING_COLOR = 0x6905F5;

    public static void unenchant(ItemStack stack, Holder<Enchantment> enchantment) {
        ItemEnchantments enchantments = stack.getTagEnchantments();
        if (enchantments.isEmpty()) {
            return;
        }

        EnchantmentHelper.updateEnchantments(stack, enchantmentMap -> enchantmentMap.removeIf(entry -> Objects.equals(entry.getKey(), enchantment.getKey())));
    }

    // ItemStack#isEnchantable ignoring existing enchantments
    public static boolean isEnchantable(ItemStack stack) {
        return stack.getMaxStackSize() == 1 && stack.isDamageableItem();
    }

    public static boolean tryToApplyCurse(Player player, ItemStack stack) {
        Registry<Enchantment> enchantments = player.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        List<Holder.Reference<Enchantment>> possibleEnchantments = enchantments.holders().filter(enchantmentReference -> enchantmentReference.is(EnchantmentTags.CURSE) && stack.supportsEnchantment(enchantmentReference)).toList();
        if (possibleEnchantments.isEmpty()) {
            return false;
        }

        RandomSource random = RandomSource.create();
        Holder<Enchantment> enchantment = possibleEnchantments.get(random.nextInt(possibleEnchantments.size()));

        ApplyItemCurseEvent event = new ApplyItemCurseEvent(player, stack, enchantment);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            return false;
        }

        enchantment = event.getEnchantment();
        if (enchantment == null) {
            return false;
        }

        if ((event.isForced() || stack.supportsEnchantment(enchantment)) && stack.getEnchantmentLevel(enchantment) <= 0) {
            stack.enchant(enchantment, Objects.requireNonNull(enchantments.get(enchantment.getKey())).getMaxLevel());
            return true;
        }

        return false;
    }

    public static void mixEnchantments(ItemStack stack, RegistryAccess registryAccess) {
        RandomSource random = RandomSource.create();
        HolderLookup.RegistryLookup<Enchantment> enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        List<Map.Entry<Holder<Enchantment>, Integer>> allEnchantments = new ArrayList<>(stack.getAllEnchantments(enchantments).entrySet());
        ItemEnchantments.Mutable futureEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        List<Holder<Enchantment>> possibleEnchantments = new ArrayList<>(enchantments.listElements().toList());

        // make sure to handle curses first
        allEnchantments = allEnchantments.stream().sorted(Comparator.comparing(entry -> !entry.getKey().is(EnchantmentTags.CURSE))).toList();

        allEnchantments.forEach(entry -> {
            Holder<Enchantment> enchantment = entry.getKey();
            int level = entry.getValue();
            if (enchantment.is(EnchantmentTags.CURSE)) {
                futureEnchantments.set(enchantment, level);
                possibleEnchantments.remove(enchantment);
                return;
            }

            while (true) {
                if (possibleEnchantments.isEmpty()) {
                    futureEnchantments.set(enchantment, level);
                    break;
                }

                Holder<Enchantment> potentialEnchantment = possibleEnchantments.get(random.nextInt(possibleEnchantments.size()));
                possibleEnchantments.remove(potentialEnchantment);
                if (stack.supportsEnchantment(potentialEnchantment) && futureEnchantments.getLevel(potentialEnchantment) == 0) {
                    futureEnchantments.set(potentialEnchantment, potentialEnchantment.is(EnchantmentTags.CURSE) ? 1 : level);
                    break;
                }
            }
        });


        EnchantmentHelper.setEnchantments(stack, futureEnchantments.toImmutable());
        if (futureEnchantments.keySet().size() != allEnchantments.size()) {
            Maledicta.getInstance().logger.warn("It seems like enchantments were deleted on {}, previous enchantments: {}", stack, allEnchantments);
        }
    }

    public static boolean hasEnchantmentInHand(LivingEntity entity, Holder<Enchantment> enchantment) {
        return entity.getItemInHand(InteractionHand.MAIN_HAND).getEnchantmentLevel(enchantment) >= 1
                || entity.getItemInHand(InteractionHand.OFF_HAND).getEnchantmentLevel(enchantment) >= 1;
    }
}
