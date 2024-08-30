package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.tileentity.FluidMixerTileEntity;
import com.mrcrayfish.vehicle.util.TileEntityUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Author: MrCrayfish
 */
public class FluidMixerBlock extends RotatedObjectBlock implements EntityBlock
{
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public FluidMixerBlock()
    {
        super(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(1.0F));
        this.registerDefaultState(this.getStateDefinition().any().setValue(DIRECTION, Direction.NORTH).setValue(ENABLED, false));
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player playerEntity, InteractionHand hand, BlockHitResult result)
    {
        if(!world.isClientSide)
        {
            if(!FluidUtil.interactWithFluidHandler(playerEntity, hand, world, pos, result.getDirection()))
            {
                BlockEntity tileEntity = world.getBlockEntity(pos);
                if(tileEntity instanceof MenuProvider)
                {
                    TileEntityUtil.sendUpdatePacket(tileEntity, (ServerPlayer) playerEntity);
                    NetworkHooks.openScreen((ServerPlayer) playerEntity, (MenuProvider) tileEntity, pos);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if(state.getBlock() != newState.getBlock())
        {
            BlockEntity tileentity = worldIn.getBlockEntity(pos);
            if(tileentity instanceof Container)
            {
                Containers.dropContents(worldIn, pos, (Container) tileentity);
                worldIn.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }




    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(ENABLED);
    }

    @Nullable
    @Override
    public FluidMixerTileEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new FluidMixerTileEntity(blockPos,blockState);
    }
}