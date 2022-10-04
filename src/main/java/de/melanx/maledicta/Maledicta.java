package de.melanx.maledicta;

import de.melanx.maledicta.network.ModNetwork;
import de.melanx.maledicta.registration.ModBlocks;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.moddingx.libx.mod.ModXRegistration;
import org.moddingx.libx.registration.RegistrationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

@Mod("maledicta")
public final class Maledicta extends ModXRegistration {

    private static Maledicta instance;
    private static ModNetwork network;
    public final Logger logger = LoggerFactory.getLogger(Maledicta.class);

    public Maledicta() {
        super(new CreativeModeTab("maledicta") {
            @Nonnull
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(ModBlocks.maledictusAufero);
            }
        });

        instance = this;
        network = new ModNetwork(this);

        MinecraftForge.EVENT_BUS.register(new EventListener());
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

    @Override
    protected void initRegistration(RegistrationBuilder builder) {
        builder.enableRegistryTracking();
    }
}
