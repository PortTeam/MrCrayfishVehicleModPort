package com.mrcrayfish.vehicle.tileentity;

import com.mrcrayfish.obfuscate.common.data.SyncedPlayerData;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.util.HermiteInterpolator;
import com.mrcrayfish.vehicle.init.ModDataKeys;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.util.TileEntityUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class GasPumpTileEntity extends TileEntitySynced implements BlockEntityTicker
{
    private int fuelingEntityId;
    private Player fuelingEntity;

    private HermiteInterpolator cachedSpline;
    private boolean recentlyUsed;

    public GasPumpTileEntity(BlockPos state, BlockState pos)
    {
        super(ModTileEntities.GAS_PUMP.get(),pos,state);
    }

    public HermiteInterpolator getCachedSpline()
    {
        return cachedSpline;
    }

    public void setCachedSpline(HermiteInterpolator cachedSpline)
    {
        this.cachedSpline = cachedSpline;
    }

    public boolean isRecentlyUsed()
    {
        return recentlyUsed;
    }

    public void setRecentlyUsed(boolean recentlyUsed)
    {
        this.recentlyUsed = recentlyUsed;
    }

    @Nullable
    public FluidTank getTank()
    {
        BlockEntity tileEntity = this.level.getBlockEntity(this.worldPosition.below());
        if(tileEntity instanceof GasPumpTankTileEntity)
        {
            return ((GasPumpTankTileEntity) tileEntity).getFluidTank();
        }
        return null;
    }

    public Player getFuelingEntity()
    {
        return this.fuelingEntity;
    }

    public void setFuelingEntity(@Nullable Player entity)
    {
        if(!this.level.isClientSide)
        {
            if(this.fuelingEntity != null)
            {
                SyncedPlayerData.instance().set(this.fuelingEntity, ModDataKeys.GAS_PUMP, Optional.empty());
            }
            this.fuelingEntity = null;
            this.fuelingEntityId = -1;
            if(entity != null)
            {
                this.fuelingEntityId = entity.getId();
                SyncedPlayerData.instance().set(entity, ModDataKeys.GAS_PUMP, Optional.of(this.getBlockPos()));
            }
            this.syncToClient();
        }
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState, BlockEntity Bentity)
    {
        if(this.fuelingEntityId != -1)
        {
            if(this.fuelingEntity == null)
            {
                Entity entity = this.level.getEntity(this.fuelingEntityId);
                if(entity instanceof Player)
                {
                    this.fuelingEntity = (Player) entity;
                }
                else if(!this.level.isClientSide)
                {
                    this.fuelingEntityId = -1;
                    this.syncFuelingEntity();
                }
            }
        }
        else if(this.level.isClientSide && this.fuelingEntity != null)
        {
            this.fuelingEntity = null;
        }

        if(!this.level.isClientSide && this.fuelingEntity != null)
        {
            if(Math.sqrt(this.fuelingEntity.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5)) > Config.SERVER.maxHoseDistance.get() || !this.fuelingEntity.isAlive())
            {
                if(this.fuelingEntity.isAlive())
                {
                    this.level.playSound(null, this.fuelingEntity.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                SyncedPlayerData.instance().set(this.fuelingEntity, ModDataKeys.GAS_PUMP, Optional.empty());
                this.fuelingEntityId = -1;
                this.fuelingEntity = null;
                this.syncFuelingEntity();
            }
        }
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);
        if(compound.contains("FuelingEntity", CompoundTag.TAG_INT))
        {
            this.fuelingEntityId = compound.getInt("FuelingEntity");
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound)
    {
        compound.putInt("FuelingEntity", this.fuelingEntityId);
        super.saveAdditional(compound);
    }

    private void syncFuelingEntity()
    {
        CompoundTag compound = new CompoundTag();
        compound.putInt("FuelingEntity", this.fuelingEntityId);
        TileEntityUtil.sendUpdatePacket(this);
    }

    @Override
    public AABB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public double getViewDistance()
//    {
//        return 65536.0D;
//    }
    
    
    
}
