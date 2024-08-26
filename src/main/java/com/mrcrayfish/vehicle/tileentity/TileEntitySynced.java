package com.mrcrayfish.vehicle.tileentity;

import com.mrcrayfish.vehicle.util.TileEntityUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Objects;

public class TileEntitySynced extends BlockEntity
{
    public TileEntitySynced(BlockEntityType<?> tileEntityTypeIn, BlockState state, BlockPos pos)
    {
        super(tileEntityTypeIn,pos,state);
    }

    public void syncToClient()
    {
        this.setChanged();
        TileEntityUtil.sendUpdatePacket(this);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return this.saveWithFullMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket pkt)
    {
        this.load(Objects.requireNonNull(pkt.getTag()));
    }

}