package de.melanx.maledicta.util;

import de.melanx.maledicta.ModConfig;
import de.melanx.maledicta.blocks.StoredCurse;
import de.melanx.maledicta.data.DamageTypeProvider;
import de.melanx.maledicta.lightning.ColoredLightningBoltEntity;
import de.melanx.maledicta.lightning.LightningArcEntity;
import de.melanx.maledicta.network.ModNetwork;
import de.melanx.maledicta.registration.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class AuferoEffects {

    private static final int[] BREAK_COLORS = {Util.LIGHTNING_COLOR, 0xFF1A1A, 0xFF8A1A, 0x1ADCFF};

    private static int breakColor(ServerLevel level) {
        return BREAK_COLORS[level.getRandom().nextInt(BREAK_COLORS.length)];
    }

    public static List<ServerPlayer> playersNear(ServerLevel level, Vec3 center, double radius) {
        double radiusSqr = radius * radius;
        return level.players().stream()
                .filter(player -> player.isAlive() && !player.isSpectator() && player.distanceToSqr(center) <= radiusSqr)
                .sorted(Comparator.comparingDouble(player -> player.distanceToSqr(center)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Nullable
    public static ItemStack findTarget(ServerPlayer player, StoredCurse curse) {
        List<ItemStack> candidates = new ArrayList<>();
        for (EquipmentSlot slot : Util.WORN_OR_HELD) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.supportsEnchantment(curse.curse()) && stack.getEnchantmentLevel(curse.curse()) <= 0) {
                candidates.add(stack);
            }
        }

        return candidates.isEmpty() ? null : candidates.get(player.getRandom().nextInt(candidates.size()));
    }

    public static boolean jump(ServerLevel level, BlockPos rodPos, ServerPlayer player, StoredCurse curse, int color) {
        ItemStack target = AuferoEffects.findTarget(player, curse);
        if (target == null) {
            return false;
        }

        target.enchant(curse.curse(), curse.appliedLevel());
        AuferoEffects.arc(level, rodPos.getCenter(), player.position().add(0, player.getBbHeight() / 2, 0), color, true);
        return true;
    }

    public static boolean jumpToNearest(ServerLevel level, BlockPos rodPos, StoredCurse curse, int color) {
        for (ServerPlayer player : AuferoEffects.playersNear(level, rodPos.getCenter(), ModConfig.MaledictusAufero.Jump.range)) {
            if (AuferoEffects.jump(level, rodPos, player, curse, color)) {
                return true;
            }
        }

        return false;
    }

    public static void lightning(ServerLevel level, Vec3 pos, int color, boolean visualOnly) {
        ColoredLightningBoltEntity lightning = ModEntities.lightningBolt.create(level, EntitySpawnReason.EVENT);
        if (lightning == null) {
            return;
        }

        lightning.setVisualOnly(visualOnly);
        lightning.snapTo(pos);
        lightning.setColor(color);
        level.addFreshEntity(lightning);
    }

    public static void arc(ServerLevel level, Vec3 from, Vec3 to, int color, boolean playSound) {
        LightningArcEntity arc = ModEntities.lightningArc.create(level, EntitySpawnReason.EVENT);
        if (arc == null) {
            return;
        }

        arc.setup(from, to, color);
        level.addFreshEntity(arc);

        if (playSound) {
            level.playSound(null, to.x, to.y, to.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER, 0.5F, 1.6F);
        }
    }

    public static void breakRod(ServerLevel level, BlockPos pos, List<StoredCurse> curses) {
        int count = curses.size();
        Vec3 center = pos.getCenter();
        double radius = ModConfig.MaledictusAufero.Break.radius;
        List<ServerPlayer> players = AuferoEffects.playersNear(level, center, radius);

        // lightnings around the rod, which hurt and burn if that is enabled
        for (int i = 0; i < Math.min(count, 6); i++) {
            double x = center.x + (level.getRandom().nextDouble() - 0.5) * 8;
            double z = center.z + (level.getRandom().nextDouble() - 0.5) * 8;
            AuferoEffects.lightning(level, new Vec3(x, pos.getY(), z), AuferoEffects.breakColor(level), !ModConfig.MaledictusAufero.Break.lightningsStrike);
        }

        // the curses jump to the players, the closer the more, but never more than the maximum
        List<StoredCurse> remaining = new ArrayList<>(curses);
        Collections.shuffle(remaining, new Random(level.getRandom().nextLong()));
        Map<ServerPlayer, Integer> received = new HashMap<>();
        boolean progress = true;
        while (progress && !remaining.isEmpty()) {
            progress = false;
            for (ServerPlayer player : players) {
                int max = ModConfig.MaledictusAufero.Break.maxCursesPerPlayer;
                double distance = Math.sqrt(player.distanceToSqr(center));
                int allowed = Math.clamp((int) Math.ceil(max * (1 - distance / radius)), 1, max);
                if (received.getOrDefault(player, 0) >= allowed) {
                    continue;
                }

                Iterator<StoredCurse> iterator = remaining.iterator();
                while (iterator.hasNext()) {
                    if (AuferoEffects.jump(level, pos, player, iterator.next(), AuferoEffects.breakColor(level))) {
                        iterator.remove();
                        received.merge(player, 1, Integer::sum);
                        progress = true;
                        break;
                    }
                }
            }
        }

        float power = (float) Math.min(count * ModConfig.MaledictusAufero.Break.explosionPowerPerCurse, 4.0);
        if (power > 0) {
            DamageSource source = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeProvider.AUFERO));
            Level.ExplosionInteraction interaction = ModConfig.MaledictusAufero.Break.explosionDamagesBlocks ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE;
            level.explode(null, source, null, center.x, center.y, center.z, power, false, interaction);
        }

        int darkTicks = count * ModConfig.MaledictusAufero.Break.darkSkyTicksPerCurse;
        for (ServerPlayer player : AuferoEffects.playersNear(level, center, radius * 2)) {
            ModNetwork.darkSky(player, darkTicks);
        }

        // what nobody could take falls from the sky as enchanted books
        double y = Math.min(pos.getY() + 64, level.getMaxY() - 1);
        for (StoredCurse curse : remaining) {
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            book.enchant(curse.curse(), curse.appliedLevel());

            double x = center.x + (level.getRandom().nextDouble() - 0.5) * 2;
            double z = center.z + (level.getRandom().nextDouble() - 0.5) * 2;
            ItemEntity item = new ItemEntity(level, x, y, z, book);
            item.setDeltaMovement(Vec3.ZERO);
            item.setGlowingTag(true);
            level.addFreshEntity(item);
        }
    }
}
