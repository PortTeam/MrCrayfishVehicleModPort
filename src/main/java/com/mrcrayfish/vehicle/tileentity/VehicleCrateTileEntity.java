package com.mrcrayfish.vehicle.tileentity;

import com.mrcrayfish.vehicle.block.VehicleCrateBlock;
import com.mrcrayfish.vehicle.client.VehicleHelper;
import com.mrcrayfish.vehicle.common.VehicleRegistry;
import com.mrcrayfish.vehicle.entity.EngineTier;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.init.ModSounds;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.item.EngineItem;
import com.mrcrayfish.vehicle.util.CommonUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Author: MrCrayfish
 */
public class VehicleCrateTileEntity extends TileEntitySynced implements BlockEntityTicker
{
    private static final Random RAND = new Random();

    private ResourceLocation entityId;
    private int color = VehicleEntity.DYE_TO_COLOR[0];
    private ItemStack engineStack = ItemStack.EMPTY;
    private ItemStack wheelStack = ItemStack.EMPTY;
    private boolean opened = false;
    private int timer;
    private UUID opener;

    @OnlyIn(Dist.CLIENT)
    private Entity entity;

    public VehicleCrateTileEntity(BlockPos state, BlockState pos)
    {
        super(ModTileEntities.VEHICLE_CRATE.get(),pos,state);
    }

    public void setEntityId(ResourceLocation entityId)
    {
        this.entityId = entityId;
        this.setChanged();
    }

    public ResourceLocation getEntityId()
    {
        return entityId;
    }

    public void open(UUID opener)
    {
        if(this.entityId != null)
        {
            this.opened = true;
            this.opener = opener;
            this.syncToClient();
        }
    }

    public boolean isOpened()
    {
        return opened;
    }

    public int getTimer()
    {
        return timer;
    }

    @OnlyIn(Dist.CLIENT)
    public <E extends Entity> E getEntity()
    {
        return (E) entity;
    }

    @Override
    public void tick(Level lv, BlockPos bp, BlockState bs, BlockEntity be)
    {
        if(this.opened)
        {
            this.timer += 5;
            if(this.level != null && this.level.isClientSide())
            {
                if(this.entityId != null && this.entity == null)
                {
                    EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(this.entityId);
                    if(entityType != null)
                    {
                        this.entity = entityType.create(this.level);
                        if(this.entity != null)
                        {
                            VehicleHelper.playSound(SoundEvents.ITEM_BREAK, this.worldPosition, RandomSource.create(),1.0F, 0.5F);
                            List<SynchedEntityData.DataValue<?>> entryList = this.entity.getEntityData().getNonDefaultValues();
                            if(entryList != null)
                            {
                                entryList.forEach(dataEntry -> this.entity.onSyncedDataUpdated(Collections.singletonList(dataEntry)));
                            }
                            if(this.entity instanceof VehicleEntity)
                            {
                                VehicleEntity vehicleEntity = (VehicleEntity) this.entity;
                                vehicleEntity.setColor(this.color);
                                if(!this.wheelStack.isEmpty())
                                {
                                    vehicleEntity.setWheelStack(this.wheelStack);
                                }
                            }
                            if(this.entity instanceof PoweredVehicleEntity)
                            {
                                PoweredVehicleEntity entityPoweredVehicle = (PoweredVehicleEntity) this.entity;
                                if(this.engineStack != null)
                                {
                                    entityPoweredVehicle.setEngineStack(this.engineStack);
                                }
                            }
                        }
                        else
                        {
                            this.entityId = null;
                        }
                    }
                    else
                    {
                        this.entityId = null;
                    }
                }
                if(this.timer == 90 || this.timer == 110 || this.timer == 130 || this.timer == 150)
                {
                    float pitch = (float) (0.9F + 0.2F * RAND.nextDouble());
                    VehicleHelper.playSound(ModSounds.BLOCK_VEHICLE_CRATE_PANEL_LAND.get(), this.worldPosition, RandomSource.create(),1.0F, pitch);
                }
                if(this.timer == 150)
                {
                    VehicleHelper.playSound(SoundEvents.GENERIC_EXPLODE, this.worldPosition,RandomSource.create(), 1.0F, 1.0F);
                    this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, false, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, 0, 0, 0);
                }
            }
            if(!this.level.isClientSide && this.timer > 250)
            {
                BlockState state = this.level.getBlockState(this.worldPosition);
                Direction facing = state.getValue(VehicleCrateBlock.DIRECTION);
                EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(this.entityId);
                if(entityType != null)
                {
                    Entity entity = entityType.create(this.level);
                    if(entity != null)
                    {
                        if(entity instanceof VehicleEntity)
                        {
                            VehicleEntity vehicleEntity = (VehicleEntity) entity;
                            vehicleEntity.setColor(this.color);
                            if(!this.wheelStack.isEmpty())
                            {
                                vehicleEntity.setWheelStack(this.wheelStack);
                            }
                        }
                        if(this.opener != null && entity instanceof PoweredVehicleEntity)
                        {
                            PoweredVehicleEntity poweredVehicle = (PoweredVehicleEntity) entity;
                            poweredVehicle.setOwner(this.opener);
                            if(!this.engineStack.isEmpty())
                            {
                                poweredVehicle.setEngineStack(this.engineStack);
                            }
                        }
                        entity.absMoveTo(this.worldPosition.getX() + 0.5, this.worldPosition.getY(), this.worldPosition.getZ() + 0.5, facing.get2DDataValue() * 90F + 180F, 0F);
                        entity.setYHeadRot(facing.get2DDataValue() * 90F + 180F);
                        this.level.addFreshEntity(entity);
                    }
                    this.level.setBlockAndUpdate(this.worldPosition, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load( compound);
        if(compound.contains("Vehicle", CompoundTag.TAG_STRING))
        {
            this.entityId = new ResourceLocation(compound.getString("Vehicle"));
        }
        if(compound.contains("Color", CompoundTag.TAG_INT))
        {
            this.color = compound.getInt("Color");
        }
        if(compound.contains("EngineStack", CompoundTag.TAG_COMPOUND))
        {
            this.engineStack = ItemStack.of(compound.getCompound("EngineStack"));
        }
        else if(compound.getBoolean("Creative"))
        {
            VehicleProperties properties = VehicleProperties.get(this.entityId);
            EngineItem engineItem = VehicleRegistry.getEngineItem(properties.getExtended(PoweredProperties.class).getEngineType(), EngineTier.IRON);
            this.engineStack = engineItem != null ? new ItemStack(engineItem) : ItemStack.EMPTY;
        }
        if(compound.contains("WheelStack", CompoundTag.TAG_COMPOUND))
        {
            this.wheelStack = ItemStack.of(compound.getCompound("WheelStack"));
        }
        else
        {
            this.wheelStack = new ItemStack(ModItems.STANDARD_WHEEL.get());
        }
        if(compound.contains("Opener", CompoundTag.TAG_STRING))
        {
            this.opener = compound.getUUID("Opener");
        }
        if(compound.contains("Opened", CompoundTag.TAG_BYTE))
        {
            this.opened = compound.getBoolean("Opened");
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound)
    {
        if(this.entityId != null)
        {
            compound.putString("Vehicle", this.entityId.toString());
        }
        if(this.opener != null)
        {
            compound.putUUID("Opener", this.opener);
        }
        if(!this.engineStack.isEmpty())
        {
            CommonUtils.writeItemStackToTag(compound, "EngineStack", this.engineStack);
        }
        if(!this.wheelStack.isEmpty())
        {
            CommonUtils.writeItemStackToTag(compound, "WheelStack", this.wheelStack);
        }
        compound.putInt("Color", this.color);
        compound.putBoolean("Opened", this.opened);
        super.saveAdditional(compound);
    }

    @Override
    public AABB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    @OnlyIn(Dist.CLIENT)
    public double getViewDistance()
    {
        return 65536.0D;
    }
}