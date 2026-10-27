package de.melanx.maledicta.registration;

import de.melanx.maledicta.structure.FortressRodRoom;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "STRUCTURE_PIECE")
public class ModStructurePieces {

    public static final StructurePieceType fortressRodRoom = (StructurePieceType.ContextlessType) FortressRodRoom::new;
}
