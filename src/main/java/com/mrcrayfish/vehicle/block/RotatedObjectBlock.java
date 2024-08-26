package com.mrcrayfish.vehicle.block;


import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Author: MrCrayfish
 */
public abstract class RotatedObjectBlock extends ObjectBlock
{
    public static final DirectionProperty DIRECTION = HorizontalDirectionalBlock.FACING;

    public RotatedObjectBlock(Block.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(DIRECTION, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return super.getStateForPlacement(context).setValue(DIRECTION, context.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(DIRECTION);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation)
    {
        return state.setValue(DIRECTION, rotation.rotate(state.getValue(DIRECTION)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror)
    {
        return state.rotate(mirror.getRotation(state.getValue(DIRECTION)));
    }

}
