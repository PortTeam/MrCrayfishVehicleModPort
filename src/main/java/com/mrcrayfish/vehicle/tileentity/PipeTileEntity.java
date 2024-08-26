package com.mrcrayfish.vehicle.tileentity;

import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: MrCrayfish
 */
public class PipeTileEntity extends TileEntitySynced
{
    protected Set<BlockPos> pumps = new HashSet<>();
    protected boolean[] disabledConnections = new boolean[Direction.values().length];

    public PipeTileEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.FLUID_PIPE.get(),state,pos);
    }
    public PipeTileEntity(BlockEntityType<?> type,BlockPos pos, BlockState state)
    {
        super(type,state,pos);
    }


    public void addPump(BlockPos pos)
    {
        this.pumps.add(pos);
    }

    public void removePump(BlockPos pos)
    {
        this.pumps.remove(pos);
    }

    public Set<BlockPos> getPumps()
    {
        return this.pumps;
    }

    public boolean[] getDisabledConnections()
    {
        return this.disabledConnections;
    }

    public void setConnectionState(Direction direction, boolean state)
    {
        this.disabledConnections[direction.get3DDataValue()] = state;
        this.syncDisabledConnections();
    }

    public boolean isConnectionDisabled(Direction direction)
    {
        return this.disabledConnections[direction.get3DDataValue()];
    }

    public void syncDisabledConnections()
    {
        if(this.level != null && !this.level.isClientSide())
        {
            CompoundTag compound = new CompoundTag();
            this.writeConnections(compound);
            TileEntityUtil.sendUpdatePacket(this, super.saveWithFullMetadata());
        }
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);
        if(compound.contains("DisabledConnections", CompoundTag.TAG_BYTE_ARRAY))
        {
            byte[] connections = compound.getByteArray("DisabledConnections");
            for(int i = 0; i < connections.length; i++)
            {
                this.disabledConnections[i] = connections[i] == (byte) 1;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        this.writeConnections(pTag);
        super.saveAdditional(pTag);
    }

    private void writeConnections(CompoundTag compound)
    {
        byte[] connections = new byte[this.disabledConnections.length];
        for(int i = 0; i < connections.length; i++)
        {
            connections[i] = (byte) (this.disabledConnections[i] ? 1 : 0);
        }
        compound.putByteArray("DisabledConnections", connections);
    }
}
