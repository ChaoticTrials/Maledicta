package de.melanx.maledicta;

import de.melanx.maledicta.data.*;
import de.melanx.maledicta.network.ModNetwork;
import de.melanx.maledicta.registration.ModCreativeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.moddingx.libx.datagen.DatagenSystem;
import org.moddingx.libx.mod.ModXRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

@Mod("maledicta")
public final class Maledicta extends ModXRegistration {

    private static Maledicta instance;
    private static ModNetwork network;
    private static ModCreativeTab creativeTab;
    public final Logger logger = LoggerFactory.getLogger(Maledicta.class);

    public Maledicta(IEventBus modBus, Dist dist) {
        instance = this;
        creativeTab = new ModCreativeTab(this);
        network = new ModNetwork(this);

        DatagenSystem.create(this, system -> {
            system.addRegistryProvider(BlockLootProvider::new);
            system.addRegistryProvider(DamageTypeProvider::new);
            system.addRegistryProvider(EnchantmentProvider::new);

            system.addDataProvider(BlockStateProvider::new);
            system.addDataProvider(EnchantmentTagsProvider::new);
            system.addDataProvider(ItemModelProvider::new);
            system.addDataProvider(RecipeProvider::new);
            system.addDataProvider(ItemTagsProvider::new);
        });
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        // NO-OP
    }

    @Override
    protected void clientSetup(FMLClientSetupEvent event) {
        // NO-OP
    }

    @Nonnull
    public static Maledicta getInstance() {
        return instance;
    }

    @Nonnull
    public static ModNetwork getNetwork() {
        return network;
    }

    @Nonnull
    public static ModCreativeTab getCreativeTab() {
        return creativeTab;
    }
}
