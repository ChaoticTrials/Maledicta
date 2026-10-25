package de.melanx.maledicta.blocks;

import de.melanx.maledicta.ModConfig;
import de.melanx.maledicta.api.MaledictusAuferoEvent;
import de.melanx.maledicta.capabilities.EnergyCollector;
import de.melanx.maledicta.lightning.ColoredLightningBoltEntity;
import de.melanx.maledicta.network.ModNetwork;
import de.melanx.maledicta.registration.ModBlockEntities;
import de.melanx.maledicta.registration.ModDataComponentTypes;
import de.melanx.maledicta.registration.ModEntities;
import de.melanx.maledicta.util.Util;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class MaledictusAufero extends LightningRodBlock implements Registerable, EntityBlock {

    protected final ModX mod;
    private final Item item;

    public MaledictusAufero(ModX mod, Properties properties, Item.Properties itemProperties) {
        super(properties);
        this.mod = mod;
        //noinspection ConstantConditions
        this.item = new BlockItem(this, itemProperties.useBlockDescriptionPrefix());
    }

    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new MaledictusAuferoBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.maledictusAufero) {
            return null;
        }

        return (tickLevel, pos, tickState, blockEntity) -> ((MaledictusAuferoBlockEntity) blockEntity).serverTick((ServerLevel) tickLevel, pos);
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
        HolderLookup.RegistryLookup<Enchantment> enchantmentRegistryLookup = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        List<ItemEntity> cursedItems = items.stream()
                .filter(item -> item.getItem()
                        .getAllEnchantments(enchantmentRegistryLookup)
                        .entrySet()
                        .stream()
                        .anyMatch(entry -> entry.getKey().is(EnchantmentTags.CURSE)))
                .toList();

        if (NeoForge.EVENT_BUS.post(new MaledictusAuferoEvent(level, state, pos, items, cursedItems)).isCanceled()) {
            return;
        }

        if (ModConfig.onlyTransferCurses) {
            if (cursedItems.isEmpty()) {
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
                LeveledEnchantment firstCurse = this.getRandomCurse(first.getItem(), level.getRandom(), enchantmentRegistryLookup);
                LeveledEnchantment secondCurse = this.getRandomCurse(second.getItem(), level.getRandom(), enchantmentRegistryLookup);

                boolean enchantFirst = false;
                boolean enchantSecond = false;
                if (secondCurse != null) {
                    enchantFirst = first.getItem().getEnchantmentLevel(secondCurse.enchantment) == 0 && first.getItem().supportsEnchantment(secondCurse.enchantment);
                }

                if (firstCurse != null) {
                    enchantSecond = second.getItem().getEnchantmentLevel(firstCurse.enchantment) == 0 && second.getItem().supportsEnchantment(firstCurse.enchantment);
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

        MaledictusAuferoBlockEntity rod = level instanceof ServerLevel && level.getBlockEntity(pos) instanceof MaledictusAuferoBlockEntity be ? be : null;
        Set<UUID> hasLightning = new HashSet<>();
        for (ItemEntity item : cursedItems) {
            LeveledEnchantment randomCurse = this.getRandomCurse(item.getItem(), level.getRandom(), enchantmentRegistryLookup);
            if (randomCurse == null || rod == null || !rod.store((ServerLevel) level, randomCurse.enchantment, randomCurse.level)) {
                continue;
            }

            Util.unenchant(item.getItem(), randomCurse.enchantment);
            ModNetwork.updateItemEnchantments(item);

            hasLightning.add(item.getUUID());
            this.summonLightning(level, item.position());
        }

        this.handleNegativeEnergy(level, hasLightning, items);
    }

    private LeveledEnchantment getRandomCurse(ItemStack stack, RandomSource random, HolderLookup.RegistryLookup<Enchantment> enchantmentRegistryLookup) {
        ItemEnchantments allEnchantments = stack.getAllEnchantments(enchantmentRegistryLookup);
        Object2IntOpenHashMap<Holder<Enchantment>> curses = allEnchantments.entrySet()
                .stream()
                .filter(entry -> entry.getKey().is(EnchantmentTags.CURSE))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum, Object2IntOpenHashMap::new));

        if (curses.isEmpty()) {
            return null;
        }

        int randomIndex = random.nextInt(curses.size());
        Holder<Enchantment> randomCurse = curses.keySet().stream().toList().get(randomIndex);

        return new LeveledEnchantment(randomCurse, curses.getInt(randomCurse));
    }

    private void handleNegativeEnergy(Level level, Set<UUID> noLightning, List<ItemEntity> items) {
        if (ModConfig.NegativeEnergy.enabled) {
            for (ItemEntity item : items) {
                ItemStack stack = item.getItem();
                if (stack.has(ModDataComponentTypes.energyCollector)) {
                    EnergyCollector energyCollector = stack.getOrDefault(ModDataComponentTypes.energyCollector, EnergyCollector.EMPTY);
                    energyCollector.setEnergy(energyCollector.negativeEnergy().get() / 2);
                    ModNetwork.updateItemEnchantments(item);

                    if (!noLightning.contains(item.getUUID())) {
                        ColoredLightningBoltEntity lightning = ModEntities.lightningBolt.create(level, EntitySpawnReason.EVENT);
                        //noinspection ConstantConditions
                        lightning.setVisualOnly(true);
                        lightning.snapTo(item.position());
                        lightning.setColor(0x00FF00);
                        level.addFreshEntity(lightning);
                    }
                }
            }
        }
    }

    private void summonLightning(Level level, Vec3 pos) {
        ColoredLightningBoltEntity lightning = ModEntities.lightningBolt.create(level, EntitySpawnReason.EVENT);
        //noinspection ConstantConditions
        lightning.setVisualOnly(true);
        lightning.snapTo(pos);
        lightning.setColor(Util.LIGHTNING_COLOR);
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

    record LeveledEnchantment(Holder<Enchantment> enchantment, int level) {}
}
