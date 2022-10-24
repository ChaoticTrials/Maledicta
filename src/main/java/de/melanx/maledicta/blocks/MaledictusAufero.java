package de.melanx.maledicta.blocks;

import de.melanx.maledicta.ModConfig;
import de.melanx.maledicta.api.MaledictusAuferoEvent;
import de.melanx.maledicta.capabilities.EnergyCollectorImpl;
import de.melanx.maledicta.lightning.LightningHelper;
import de.melanx.maledicta.network.ModNetwork;
import de.melanx.maledicta.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Triple;
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
        this.item = new BlockItem(this, itemProperties.tab(mod.tab));
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

        if (ModConfig.NegativeEnergy.enabled) {
            for (ItemEntity item : items) {
                item.getItem().getCapability(EnergyCollectorImpl.INSTANCE).ifPresent(cap -> {
                    cap.setEnergy(cap.negativeEnergy().get() / 2);
                    ModNetwork.updateItemEnchantments(item);

                    LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                    //noinspection ConstantConditions
                    lightning.setVisualOnly(true);
                    lightning.moveTo(item.position());
                    LightningHelper.setColor(lightning, "00FF00");
                    level.addFreshEntity(lightning);
                });
            }
        }

        Set<Triple<UUID, Enchantment, Integer>> collectedCurses = new HashSet<>();
        cursedItems.forEach(item -> {
            Map<Enchantment, Integer> allEnchantments = item.getItem().getAllEnchantments();
            List<Map.Entry<Enchantment, Integer>> curses = allEnchantments.entrySet().stream().filter(entry -> entry.getKey().isCurse()).toList();
            Map.Entry<Enchantment, Integer> randomCurse = curses.get(level.random.nextInt(curses.size()));
            collectedCurses.add(Triple.of(item.getUUID(), randomCurse.getKey(), randomCurse.getValue()));
            Util.unenchant(item.getItem(), randomCurse.getKey());
            ModNetwork.updateItemEnchantments(item);

            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
            //noinspection ConstantConditions
            lightning.setVisualOnly(true);
            lightning.moveTo(item.position());
            LightningHelper.setColor(lightning, Util.LIGHTNING_COLOR);
            level.addFreshEntity(lightning);
        });

        collectedCurses.forEach(curse -> {
            ItemEntity randomItem = items.get(level.random.nextInt(items.size()));
            if (randomItem.getUUID() != curse.getLeft() && curse.getMiddle().canEnchant(randomItem.getItem()) && randomItem.getItem().getEnchantmentLevel(curse.getMiddle()) < 1) {
                randomItem.getItem().enchant(curse.getMiddle(), curse.getRight());
                ModNetwork.updateItemEnchantments(randomItem);
            }
            items.remove(randomItem);
        });
    }

    public static AABB expandBox(Vec3 center, double radius) {
        return new AABB(center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);
    }

    @Override
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        builder.register(Registry.ITEM_REGISTRY, this.item);
    }
}
