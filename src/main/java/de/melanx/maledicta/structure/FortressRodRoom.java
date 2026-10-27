package de.melanx.maledicta.structure;

import de.melanx.maledicta.ModConfig;
import de.melanx.maledicta.blocks.MaledictusAuferoBlockEntity;
import de.melanx.maledicta.data.ChestLootProvider;
import de.melanx.maledicta.data.EnchantmentTagsProvider;
import de.melanx.maledicta.registration.ModBlocks;
import de.melanx.maledicta.registration.ModStructurePieces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FortressRodRoom extends NetherFortressPieces.NetherBridgePiece {

    private static final int WIDTH = 9;
    private static final int HEIGHT = 8;
    private static final int DEPTH = 9;

    public FortressRodRoom(int genDepth, BoundingBox boundingBox, Direction direction) {
        super(ModStructurePieces.fortressRodRoom, genDepth, boundingBox);
        this.setOrientation(direction);
    }

    public FortressRodRoom(CompoundTag tag) {
        super(ModStructurePieces.fortressRodRoom, tag);
    }

    @Nullable
    public static FortressRodRoom createPiece(StructurePieceAccessor structurePieceAccessor, int footX, int footY, int footZ, Direction direction, int genDepth) {
        BoundingBox box = BoundingBox.orientBox(footX, footY, footZ, -3, 0, 0, WIDTH, HEIGHT, DEPTH, direction);
        return isOkBox(box) && structurePieceAccessor.findCollisionPiece(box) == null
                ? new FortressRodRoom(genDepth, box, direction)
                : null;
    }

    @Override
    public void postProcess(@Nonnull WorldGenLevel level, @Nonnull StructureManager structureManager, @Nonnull ChunkGenerator generator, @Nonnull RandomSource random, @Nonnull BoundingBox chunkBB, @Nonnull ChunkPos chunkPos, @Nonnull BlockPos referencePos) {
        BlockState bricks = Blocks.NETHER_BRICKS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // floor, walls and ceiling
        this.generateBox(level, chunkBB, 0, 0, 0, 8, 1, 8, bricks, bricks, false);
        this.generateBox(level, chunkBB, 1, 2, 1, 7, 6, 7, air, air, false);
        this.generateBox(level, chunkBB, 0, 2, 0, 0, 6, 8, bricks, bricks, false);
        this.generateBox(level, chunkBB, 8, 2, 0, 8, 6, 8, bricks, bricks, false);
        this.generateBox(level, chunkBB, 1, 2, 0, 7, 6, 0, bricks, bricks, false);
        this.generateBox(level, chunkBB, 1, 2, 8, 7, 6, 8, bricks, bricks, false);

        // the entrance, as big as a castle corridor
        this.generateBox(level, chunkBB, 3, 2, 0, 5, 5, 0, air, air, false);

        // windows in the side walls, like the castle corridors have
        BlockState nsFence = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true).setValue(FenceBlock.SOUTH, true);
        for (int z : new int[]{2, 6}) {
            this.generateBox(level, chunkBB, 0, 3, z, 0, 4, z, nsFence, nsFence, false);
            this.generateBox(level, chunkBB, 8, 3, z, 8, 4, z, nsFence, nsFence, false);
        }

        // the marker: soul fire in the corners and soul lanterns around the rod
        for (int x : new int[]{1, 7}) {
            for (int z : new int[]{1, 7}) {
                this.placeBlock(level, Blocks.SOUL_SAND.defaultBlockState(), x, 1, z, chunkBB);
                this.placeBlock(level, Blocks.SOUL_FIRE.defaultBlockState(), x, 2, z, chunkBB);
            }
        }

        BlockState lantern = Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
        this.placeBlock(level, lantern, 4, 5, 7, chunkBB);
        this.placeBlock(level, lantern, 1, 4, 4, chunkBB);
        this.placeBlock(level, lantern, 7, 4, 4, chunkBB);
        this.placeBlock(level, lantern, 2, 4, 1, chunkBB);
        this.placeBlock(level, lantern, 6, 4, 1, chunkBB);

        this.placeBlock(level, nsFence, 4, 6, 7, chunkBB);
        this.placeBlock(level, nsFence, 1, 5, 4, chunkBB);
        this.placeBlock(level, nsFence, 7, 5, 4, chunkBB);
        this.placeBlock(level, nsFence, 2, 5, 1, chunkBB);
        this.placeBlock(level, nsFence, 6, 5, 1, chunkBB);

        // the rod on a pedestal
        this.placeBlock(level, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 4, 2, 5, chunkBB);
        this.placeRod(level, chunkBB, random, 4, 3, 5);

        this.createChest(level, chunkBB, random, 4, 2, 7, ChestLootProvider.FORTRESS_ROD_ROOM);

        this.spawnGuard(level, chunkBB, EntityType.WITHER_SKELETON, 2, 2, 5);
        this.spawnGuard(level, chunkBB, EntityType.WITHER_SKELETON, 6, 2, 5);
        this.spawnGuard(level, chunkBB, EntityType.BLAZE, 4, 4, 3);

        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                this.fillColumnDown(level, bricks, x, -1, z, chunkBB);
            }
        }
    }

    private void placeRod(WorldGenLevel level, BoundingBox chunkBB, RandomSource random, int x, int y, int z) {
        BlockPos pos = this.getWorldPos(x, y, z);
        if (!chunkBB.isInside(pos)) {
            return;
        }

        this.placeBlock(level, ModBlocks.maledictusAufero.defaultBlockState(), x, y, z, chunkBB);
        if (level.getBlockEntity(pos) instanceof MaledictusAuferoBlockEntity rod) {
            rod.prefill(level.getLevel(), new MaledictusAuferoBlockEntity.PrefillEnchantments(EnchantmentTagsProvider.PREFILLED_ROD_ENCHANTMENTS, ModConfig.FortressRoom.minCurses, ModConfig.FortressRoom.maxCurses));
            rod.setCanDecay(false);
        }
    }

    private void spawnGuard(WorldGenLevel level, BoundingBox chunkBB, EntityType<? extends Mob> type, int x, int y, int z) {
        BlockPos pos = this.getWorldPos(x, y, z);
        if (!chunkBB.isInside(pos)) {
            return;
        }

        Mob guard = type.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (guard == null) {
            return;
        }

        guard.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360, 0);
        //noinspection deprecation,OverrideOnly
        guard.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
        guard.setPersistenceRequired();
        level.addFreshEntityWithPassengers(guard);
    }
}
