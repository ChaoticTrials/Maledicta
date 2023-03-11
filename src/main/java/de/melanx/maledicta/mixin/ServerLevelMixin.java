package de.melanx.maledicta.mixin;

import de.melanx.maledicta.registration.ModPoiTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Redirect(
            method = "findLightningRod",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;findClosest(Ljava/util/function/Predicate;Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;ILnet/minecraft/world/entity/ai/village/poi/PoiManager$Occupancy;)Ljava/util/Optional;"
            )
    )
    private Optional<BlockPos> addCustomBlockToPoi(PoiManager instance, Predicate<Holder<PoiType>> typePredicate, Predicate<BlockPos> posPredicate, BlockPos pos, int distance, PoiManager.Occupancy status) {
        //noinspection OptionalGetWithoutIsPresent,deprecation
        return instance.findClosest(predicate -> predicate.is(PoiTypes.LIGHTNING_ROD) || predicate.is(BuiltInRegistries.POINT_OF_INTEREST_TYPE.getResourceKey(ModPoiTypes.poiType).get()), blockPos -> {
            return blockPos.getY() == ((ServerLevel) (Object) this).getHeight(Heightmap.Types.WORLD_SURFACE, blockPos.getX(), blockPos.getZ()) - 1;
        }, pos, 128, PoiManager.Occupancy.ANY);
    }
}
