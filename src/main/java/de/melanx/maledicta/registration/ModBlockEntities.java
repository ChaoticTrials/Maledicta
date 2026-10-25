package de.melanx.maledicta.registration;

import de.melanx.maledicta.blocks.MaledictusAuferoBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "BLOCK_ENTITY_TYPE")
public class ModBlockEntities {

    public static final BlockEntityType<MaledictusAuferoBlockEntity> maledictusAufero = new BlockEntityType<>(MaledictusAuferoBlockEntity::new, ModBlocks.maledictusAufero);
}
