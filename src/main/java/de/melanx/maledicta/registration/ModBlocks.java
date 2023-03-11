package de.melanx.maledicta.registration;

import de.melanx.maledicta.Maledicta;
import de.melanx.maledicta.blocks.MaledictusAufero;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "BLOCK", priority = -1)
public class ModBlocks {

    public static final Block maledictusAufero = new MaledictusAufero(Maledicta.getInstance(), BlockBehaviour.Properties.copy(Blocks.LIGHTNING_ROD), new Item.Properties());
}
