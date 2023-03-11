package de.melanx.maledicta.blocks;

import de.melanx.maledicta.ModConfig;
import de.melanx.maledicta.api.MaledictusAuferoEvent;
import de.melanx.maledicta.capabilities.EnergyCollectorImpl;
import de.melanx.maledicta.lightning.LightningHelper;
import de.melanx.maledicta.network.ModNetwork;
import de.melanx.maledicta.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

import javax.annotation.Nonnull;
import java.util.*;

public class MaledictusAufero extends LightningRodBlock implements Registerable {

    protected final ModX mod;
    private final Item item;

    public MaledictusAufero(ModX mod, Properties properties, Item.Properties itemProperties) {
        super(properties);
        this.mod = mod;
        //noinspection ConstantConditions
        this.item = new BlockItem(this, itemProperties);
    }

    @Override
    public void onLightningStrike(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos) {
        level.setBlock(pos, state.setValue(POWERED, true), Block.UPDATE_ALL);
        level.updateNeighborsAt(pos.relative(state.getValue(FACING).getOpposite()), this);
        level.scheduleTick(pos, this, 5);
        level.levelEvent(LevelEvent.PARTICLES_ELECTRIC_SPARK, pos, state.getValue(FACING).getAxis().ordinal());

        // cure cursed items
        List<ItemEntity> items = new ArrayList<>(level.getEntitiesOfClass(ItemEntity.class, expandBox(new Vec3(pos.getX(), pos.getY(), pos.getZ()), 3)).stream()
                .filter(item -> Util.isEnchantable(item.getItem()))
                .toList());
        List<ItemEntity> cursedItems = items.stream()
                .filter(item -> item.getItem().getAllEnchantments().entrySet().stream().anyMatch(entry -> entry.getKey().isCurse()))
                .toList();

        if (MinecraftForge.EVENT_BUS.post(new MaledictusAuferoEvent(level, state, pos, items, cursedItems))) return;

        if (ModConfig.onlyTransferCurses) {
            if (cursedItems.size() == 0) {
                return;
            }

            Set<UUID> alreadyUsed = new HashSet<>();
            Set<UUID> hasLightning = new HashSet<>();

            while (true) {
                ItemEntity first = null;
                ItemEntity second = null;

                Optional<ItemEntity> opt1 = items.stream().filter(item -> !alreadyUsed.contains(item.getUUID())).findAny();
                if (opt1.isPresent()) {
                    first = opt1.get();
                    alreadyUsed.add(opt1.get().getUUID());
                }

                Optional<ItemEntity> opt2 = items.stream().filter(item -> !alreadyUsed.contains(item.getUUID())).findAny();
                if (opt2.isPresent()) {
                    second = opt2.get();
                    alreadyUsed.add(opt2.get().getUUID());
                }

                if (first == null || second == null) {
                    break;
                }

                hasLightning.add(first.getUUID());
                hasLightning.add(second.getUUID());
                LeveledEnchantment firstCurse = this.getRandomCurse(first.getItem(), level.random);
                LeveledEnchantment secondCurse = this.getRandomCurse(second.getItem(), level.random);

                boolean enchantFirst = false;
                boolean enchantSecond = false;
                if (secondCurse != null) {
                    enchantFirst = first.getItem().getEnchantmentLevel(secondCurse.enchantment) == 0 && secondCurse.enchantment.canEnchant(first.getItem());
                }

                if (firstCurse != null) {
                    enchantSecond = second.getItem().getEnchantmentLevel(firstCurse.enchantment) == 0 && firstCurse.enchantment.canEnchant(second.getItem());
                }

                if (enchantFirst || enchantSecond) {
                    if (enchantFirst) {
                        Util.unenchant(second.getItem(), secondCurse.enchantment);
                        first.getItem().enchant(secondCurse.enchantment, secondCurse.level);
                    }

                    if (enchantSecond) {
                        Util.unenchant(first.getItem(), firstCurse.enchantment);
                        second.getItem().enchant(firstCurse.enchantment, firstCurse.level);
                    }

                    this.summonLightning(level, first.position());
                    this.summonLightning(level, second.position());

                    ModNetwork.updateItemEnchantments(first);
                    ModNetwork.updateItemEnchantments(second);
                }
            }

            this.handleNegativeEnergy(level, hasLightning, items);
            return;
        }

        Set<UUID> hasLightning = new HashSet<>();
        Set<Pair<UUID, LeveledEnchantment>> collectedCurses = new HashSet<>();
        cursedItems.forEach(item -> {
            LeveledEnchantment randomCurse = this.getRandomCurse(item.getItem(), level.random);
            if (randomCurse != null) {
                collectedCurses.add(Pair.of(item.getUUID(), randomCurse));
                Util.unenchant(item.getItem(), randomCurse.enchantment);
                ModNetwork.updateItemEnchantments(item);

                hasLightning.add(item.getUUID());
                this.summonLightning(level, item.position());
            }
        });

        collectedCurses.forEach(curse -> {
            ItemEntity randomItem = items.get(level.random.nextInt(items.size()));
            if (randomItem.getUUID() != curse.getLeft() && curse.getValue().enchantment.canEnchant(randomItem.getItem()) && randomItem.getItem().getEnchantmentLevel(curse.getValue().enchantment) < 1) {
                randomItem.getItem().enchant(curse.getValue().enchantment, curse.getValue().level);
                ModNetwork.updateItemEnchantments(randomItem);
            }
            items.remove(randomItem);
        });

        this.handleNegativeEnergy(level, hasLightning, items);
    }

    private LeveledEnchantment getRandomCurse(ItemStack stack, RandomSource random) {
        Map<Enchantment, Integer> allEnchantments = stack.getAllEnchantments();
        List<Map.Entry<Enchantment, Integer>> curses = allEnchantments.entrySet().stream().filter(entry -> entry.getKey().isCurse()).toList();

        if (curses.isEmpty()) {
            return null;
        }

        Map.Entry<Enchantment, Integer> randomCurse = curses.get(random.nextInt(curses.size()));

        return new LeveledEnchantment(randomCurse.getKey(), randomCurse.getValue());
    }

    private void handleNegativeEnergy(Level level, Set<UUID> noLightning, List<ItemEntity> items) {
        if (ModConfig.NegativeEnergy.enabled) {
            for (ItemEntity item : items) {
                item.getItem().getCapability(EnergyCollectorImpl.INSTANCE).ifPresent(cap -> {
                    cap.setEnergy(cap.negativeEnergy().get() / 2);
                    ModNetwork.updateItemEnchantments(item);

                    if (!noLightning.contains(item.getUUID())) {
                        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                        //noinspection ConstantConditions
                        lightning.setVisualOnly(true);
                        lightning.moveTo(item.position());
                        LightningHelper.setColor(lightning, 0x00FF00);
                        level.addFreshEntity(lightning);
                    }
                });
            }
        }
    }

    private void summonLightning(Level level, Vec3 pos) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        //noinspection ConstantConditions
        lightning.setVisualOnly(true);
        lightning.moveTo(pos);
        LightningHelper.setColor(lightning, Util.LIGHTNING_COLOR);
        level.addFreshEntity(lightning);
    }

    public static AABB expandBox(Vec3 center, double radius) {
        return new AABB(center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);
    }

    @Override
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        builder.register(Registries.ITEM, this.item);
    }

    record LeveledEnchantment(Enchantment enchantment, int level) {
        // NO-OP
    }
}
