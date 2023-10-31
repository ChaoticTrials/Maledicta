package de.melanx.maledicta;

import de.melanx.maledicta.data.*;
import de.melanx.maledicta.network.ModNetwork;
import de.melanx.maledicta.registration.ModCreativeTab;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.moddingx.libx.datagen.DatagenSystem;
import org.moddingx.libx.mod.ModXRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

@Mod("maledicta")
public final class Maledicta extends ModXRegistration {

    private static Maledicta instance;
    private static ModNetwork network;
    public final Logger logger = LoggerFactory.getLogger(Maledicta.class);

    public Maledicta() {
        instance = this;
        network = new ModNetwork(this);

        MinecraftForge.EVENT_BUS.register(new EventListener());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModCreativeTab::onCreateTabs);

        DatagenSystem.create(this, system -> {
            system.addDataProvider(BlockLootProvider::new);
            system.addDataProvider(BlockStateProvider::new);
            system.addDataProvider(ItemModelProvider::new);
            system.addDataProvider(RecipeProvider::new);

            system.addRegistryProvider(DamageTypeProvider::new);
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
}
