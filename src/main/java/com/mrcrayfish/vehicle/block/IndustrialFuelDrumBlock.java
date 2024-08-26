package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.tileentity.IndustrialFuelDrumTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Author: MrCrayfish
 */
public class IndustrialFuelDrumBlock extends FuelDrumBlock implements EntityBlock
{
    @Override
    public int getCapacity()
    {
        return Config.SERVER.industrialFuelDrumCapacity.get();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new IndustrialFuelDrumTileEntity(blockPos,blockState);
    }
}
