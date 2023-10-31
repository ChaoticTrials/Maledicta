package de.melanx.maledicta.registration;

import de.melanx.maledicta.Maledicta;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "CREATIVE_MODE_TAB")
public class ModCreativeTab {

    public static final CreativeModeTab maledictaTab = CreativeModeTab.builder()
            .title(Component.literal("Maledicta"))
            .icon(() -> new ItemStack(ModBlocks.maledictusAufero))
            .build();

    public static void onCreateTabs(BuildCreativeModeTabContentsEvent event) {
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            //noinspection DataFlowIssue
            if (Maledicta.getInstance().modid.equals(ForgeRegistries.ITEMS.getKey(item).getNamespace())) {
                event.accept(item);
            }
        }
    }
}
