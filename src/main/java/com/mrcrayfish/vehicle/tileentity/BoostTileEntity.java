package com.mrcrayfish.vehicle.tileentity;

import com.mrcrayfish.vehicle.init.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;


/**
 * Author: MrCrayfish
 */
public class BoostTileEntity extends TileEntitySynced
{
    private float speedMultiplier;

    public BoostTileEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.BOOST.get(),state,pos);
    }

    public BoostTileEntity(float defaultSpeedMultiplier,BlockPos pos, BlockState state)
    {
        super(ModTileEntities.BOOST.get(),state,pos);
        this.speedMultiplier = defaultSpeedMultiplier;
    }

    public float getSpeedMultiplier()
    {
        return speedMultiplier;
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load( compound);
        if(compound.contains("SpeedMultiplier", CompoundTag.TAG_FLOAT))
        {
            this.speedMultiplier = compound.getFloat("SpeedMultiplier");
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound)
    {
        compound.putFloat("SpeedMultiplier", this.speedMultiplier);
         super.saveAdditional(compound);
    }
}

