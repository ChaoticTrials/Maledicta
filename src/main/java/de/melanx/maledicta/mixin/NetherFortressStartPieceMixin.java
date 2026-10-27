package de.melanx.maledicta.mixin;

import de.melanx.maledicta.ModConfig;
import de.melanx.maledicta.structure.FortressRodRoom;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(NetherFortressPieces.StartPiece.class)
public abstract class NetherFortressStartPieceMixin {

    @Shadow
    @Final
    private List<NetherFortressPieces.PieceWeight> availableCastlePieces;

    @Inject(method = "<init>(Lnet/minecraft/util/RandomSource;II)V", at = @At("RETURN"))
    private void maledicta$addRodRoom(RandomSource random, int west, int north, CallbackInfo ci) {
        // an own random, so fortresses without the room are generated exactly like in vanilla
        RandomSource roomRandom = RandomSource.create(((long) west * 341873128712L + (long) north * 132897987541L) ^ 0x6D616C6564696374L);
        if (roomRandom.nextDouble() < ModConfig.FortressRoom.chance) {
            this.availableCastlePieces.add(new NetherFortressPieces.PieceWeight(FortressRodRoom.class, 20, 1));
        }
    }
}
