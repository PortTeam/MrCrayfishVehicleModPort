package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.tileentity.WorkstationTileEntity;
import com.mrcrayfish.vehicle.util.VoxelShapeHelper;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class WorkstationBlock extends RotatedObjectBlock implements EntityBlock
{
    private static final VoxelShape SHAPE = Util.make(() -> {
        List<VoxelShape> shapes = new ArrayList<>();
        shapes.add(Block.box(0, 1, 0, 16, 16, 16));
        shapes.add(Block.box(1, 0, 1, 3, 1, 3));
        shapes.add(Block.box(1, 0, 13, 3, 1, 15));
        shapes.add(Block.box(13, 0, 1, 15, 1, 3));
        shapes.add(Block.box(13, 0, 13, 15, 1, 15));
        return VoxelShapeHelper.combineAll(shapes);
    });

    public WorkstationBlock()
    {
        super(Block.Properties.copy(Blocks.IRON_BLOCK).strength(1.0F));
    }


    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState pState, Level world, BlockPos pos, Player playerEntity, InteractionHand pHand, BlockHitResult pHit)
    {
        if(!world.isClientSide)
        {
            BlockEntity tileEntity = world.getBlockEntity(pos);
            if(tileEntity instanceof MenuProvider)
            {
                NetworkHooks.openScreen((ServerPlayer) playerEntity, (MenuProvider) tileEntity, pos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new WorkstationTileEntity(blockPos,blockState);
    }
}