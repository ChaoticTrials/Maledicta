package de.melanx.maledicta.mixin;

import de.melanx.maledicta.structure.FortressRodRoom;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetherFortressPieces.class)
public abstract class NetherFortressPiecesMixin {

    @Inject(method = "findAndCreateBridgePieceFactory", at = @At("HEAD"), cancellable = true)
    private static void maledicta$createRodRoom(NetherFortressPieces.PieceWeight piece, StructurePieceAccessor structurePieceAccessor, RandomSource random, int footX, int footY, int footZ, Direction direction, int depth, CallbackInfoReturnable<NetherFortressPieces.NetherBridgePiece> cir) {
        if (piece.pieceClass == FortressRodRoom.class) {
            cir.setReturnValue(FortressRodRoom.createPiece(structurePieceAccessor, footX, footY, footZ, direction, depth));
        }
    }
}
