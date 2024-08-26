package com.mrcrayfish.vehicle.tileentity;

import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.inventory.IStorageBlock;
import com.mrcrayfish.vehicle.inventory.container.WorkstationContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;


import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class WorkstationTileEntity extends TileEntitySynced implements IStorageBlock
{
    private NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);

    public WorkstationTileEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.WORKSTATION.get(),state,pos);
    }

    @Override
    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        ContainerHelper.loadAllItems(pTag, this.inventory);
    }

    @Override
    public void saveAdditional(CompoundTag compound)
    {
        ContainerHelper.saveAllItems(compound, this.inventory);
        super.saveAdditional(compound);
    }


    @Override
    public boolean canPlaceItem(int index, ItemStack stack)
    {
        return index != 0 || (stack.getItem() instanceof DyeItem && this.inventory.get(index).getCount() < 1);
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("container.vehicle.workstation");
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new WorkstationContainer(i, inventory, this);
    }
}
