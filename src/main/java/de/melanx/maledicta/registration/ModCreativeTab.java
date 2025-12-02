package de.melanx.maledicta.registration;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.moddingx.libx.creativetab.CreativeTabX;
import org.moddingx.libx.mod.ModX;

public class ModCreativeTab extends CreativeTabX {

    public ModCreativeTab(ModX mod) {
        super(mod);
    }

    @Override
    protected void addItems(TabContext ctx) {
        this.addModItems(ctx);
    }

    @Override
    protected void buildTab(CreativeModeTab.Builder builder) {
        builder.title(Component.literal("Maledicta"))
                .icon(() -> new ItemStack(ModBlocks.maledictusAufero));
    }
}
