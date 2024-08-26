package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.tileentity.JackTileEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;


import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class JackBlock extends RotatedObjectBlock
{
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 10, 15);

    public JackBlock()
    {
        super(Block.Properties.copy(Blocks.PISTON));
        this.registerDefaultState(this.getStateDefinition().any().setValue(DIRECTION, Direction.NORTH).setValue(ENABLED, false));
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        BlockEntity tileEntity = pLevel.getBlockEntity(pPos);
        if(tileEntity instanceof JackTileEntity)
        {
            JackTileEntity jack = (JackTileEntity) tileEntity;
            return Shapes.create(SHAPE.bounds().expandTowards(0, 0.5 * jack.getProgress(), 0));
        }
        return SHAPE;
    }


    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(ENABLED);
    }

    // Prevents the tile entity from being removed if the replacement block is the same
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState replaceState, boolean what)
    {
        if(!state.is(replaceState.getBlock()))
        {
            super.onRemove(state, world, pos, replaceState, what);
        }
    }
}
