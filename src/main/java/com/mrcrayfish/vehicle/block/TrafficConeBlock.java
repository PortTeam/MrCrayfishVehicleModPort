package com.mrcrayfish.vehicle.block;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Author: MrCrayfish
 */
public class TrafficConeBlock extends ObjectBlock
{
    private static final VoxelShape COLLISION_SHAPE = Block.box(2, 0, 2, 14, 18, 14);
    private static final VoxelShape SELECTION_SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public TrafficConeBlock()
    {
        super(Properties.copy(Blocks.CLAY).strength(0.5F));
    }



    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SELECTION_SHAPE;
    }

    public static VoxelShape getCollisionShape() {
        return COLLISION_SHAPE;
    }
}
