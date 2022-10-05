package de.melanx.maledicta.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.moddingx.libx.base.BlockBase;
import org.moddingx.libx.block.RotationShape;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PurificationCrystal extends BlockBase {

    public static final RotationShape SHAPE = new RotationShape(Shapes.or(
            box(4, 0, 5, 12, 2, 7),
            box(4, 0, 9, 12, 2, 11),
            box(12, 0, 3, 13, 4, 4),
            box(3, 0, 3, 4, 4, 4),
            box(3, 0, 12, 4, 4, 13),
            box(12, 0, 12, 13, 4, 13),
            box(3, 4, 3, 13, 5, 13),
            box(13, 5, 3, 14, 9, 13),
            box(2, 5, 3, 3, 9, 13),
            box(3, 5, 2, 13, 9, 3),
            box(3, 5, 13, 13, 9, 14),
            box(3, 9, 14, 13, 13, 15),
            box(3, 9, 1, 13, 13, 2),
            box(1, 9, 3, 2, 13, 13),
            box(14, 9, 3, 15, 13, 13),
            box(13, 9, 2, 14, 13, 3),
            box(13, 9, 13, 14, 13, 14),
            box(2, 9, 13, 3, 13, 14),
            box(2, 9, 2, 3, 13, 3)
    ));

    public PurificationCrystal(ModX mod, Properties properties, @Nullable Item.Properties itemProperties) {
        super(mod, properties, itemProperties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.LIT, false)
        );
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING).add(BlockStateProperties.LIT);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return SHAPE.getShape(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
    }
}
