package com.mrcrayfish.vehicle.world.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mrcrayfish.vehicle.init.ModLootFunctions;
import com.mrcrayfish.vehicle.tileentity.IFluidTankWriter;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;

import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * Author: MrCrayfish
 */
public class CopyFluidTanks //extends LootTable
{
    /**
    private CopyFluidTanks(LootItemCondition[] conditionsIn)
    {
        super(conditionsIn);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context)
    {
        BlockState state = context.getParamOrNull(LootParameters.BLOCK_STATE);
        if(state != null && stack.getItem() == state.getBlock().asItem())
        {
            TileEntity tileEntity = context.getParamOrNull(LootParameters.BLOCK_ENTITY);
            if(tileEntity != null)
            {
                CompoundNBT tileEntityTag = new CompoundNBT();
                if(tileEntity instanceof TileFluidHandler)
                {
                    LazyOptional<IFluidHandler> handler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                    handler.ifPresent(h ->
                    {
                        FluidTank tank = (FluidTank) h;
                        if(!tank.isEmpty())
                        {
                            tank.writeToNBT(tileEntityTag);
                        }
                    });
                }
                else if(tileEntity instanceof IFluidTankWriter)
                {
                    IFluidTankWriter writer = (IFluidTankWriter) tileEntity;
                    if(!writer.areTanksEmpty())
                    {
                        writer.writeTanks(tileEntityTag);
                    }
                }

                if(!tileEntityTag.isEmpty())
                {
                    CompoundNBT compound = stack.getTag();
                    if(compound == null)
                    {
                        compound = new CompoundNBT();
                    }
                    compound.put("BlockEntityTag", tileEntityTag);
                    stack.setTag(compound);
                }
            }
        }
        return stack;
    }

    @Override
    public LootFunctionType getType()
    {
        return ModLootFunctions.COPY_FLUID_TANKS;
    }

    public static CopyFluidTanks.Builder copyFluidTanks()
    {
        return new CopyFluidTanks.Builder();
    }

    public static class Builder extends LootFunction.Builder<CopyFluidTanks.Builder>
    {
        private Builder() {}

        protected CopyFluidTanks.Builder getThis()
        {
            return this;
        }

        public ILootFunction build()
        {
            return new CopyFluidTanks(this.getConditions());
        }
    }

    public static class Serializer extends LootFunction.Serializer<CopyFluidTanks>
    {
        @Override
        public CopyFluidTanks deserialize(JsonObject object, JsonDeserializationContext deserializationContext, ILootCondition[] conditionsIn)
        {
            return new CopyFluidTanks(conditionsIn);
        }
    }
    **/
}
