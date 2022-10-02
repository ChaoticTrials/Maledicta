package de.melanx.maledicta;

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
    public final Logger logger = LoggerFactory.getLogger(Maledicta.class);

    public Maledicta() {
        super(new CreativeModeTab("maledicta") {
            @Nonnull
            @Override
            public ItemStack makeIcon() {
                return ItemStack.EMPTY;
            }
        });

        instance = this;

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

    @Override
    protected void initRegistration(RegistrationBuilder builder) {
        builder.enableRegistryTracking();
    }
}
