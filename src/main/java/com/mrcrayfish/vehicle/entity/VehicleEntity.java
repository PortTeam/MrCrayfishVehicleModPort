package com.mrcrayfish.vehicle.entity;

import com.mrcrayfish.obfuscate.common.data.SyncedPlayerData;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.block.VehicleCrateBlock;
import com.mrcrayfish.vehicle.client.VehicleHelper;
import com.mrcrayfish.vehicle.common.CosmeticTracker;
import com.mrcrayfish.vehicle.common.Seat;
import com.mrcrayfish.vehicle.common.SeatTracker;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.crafting.WorkstationRecipe;
import com.mrcrayfish.vehicle.crafting.WorkstationRecipes;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.init.ModDataKeys;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.init.ModSounds;
import com.mrcrayfish.vehicle.item.SprayCanItem;
import com.mrcrayfish.vehicle.network.datasync.VehicleDataValue;
import com.mrcrayfish.vehicle.util.CommonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Author: MrCrayfish
 */
public abstract class VehicleEntity extends Entity implements IEntityAdditionalSpawnData
{
    public static final int[] DYE_TO_COLOR = new int[] {16383998, 16351261, 13061821, 3847130, 16701501, 8439583, 15961002, 4673362, 10329495, 1481884, 8991416, 3949738, 8606770, 6192150, 11546150, 1908001};

    protected static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> TIME_SINCE_HIT = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> HEALTH = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> TRAILER = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<ItemStack> WHEEL_STACK = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.ITEM_STACK);

    protected UUID trailerId;
    protected TrailerEntity trailer = null;
    private int searchDelay = 0;

    protected int lerpSteps;
    protected double lerpX;
    protected double lerpY;
    protected double lerpZ;
    protected double lerpYaw;
    protected double lerpPitch;

    protected final SeatTracker seatTracker;
    protected final CosmeticTracker cosmeticTracker;
    protected final Map<EntityDataAccessor<?>, VehicleDataValue<?>> paramToDataValue = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    protected float bodyRotationPitch;
    @OnlyIn(Dist.CLIENT)
    protected float prevBodyRotationPitch;
    @OnlyIn(Dist.CLIENT)
    protected float bodyRotationYaw;
    @OnlyIn(Dist.CLIENT)
    protected float prevBodyRotationYaw;
    @OnlyIn(Dist.CLIENT)
    protected float bodyRotationRoll;
    @OnlyIn(Dist.CLIENT)
    protected float prevBodyRotationRoll;
    @OnlyIn(Dist.CLIENT)
    protected float passengerYawOffset;
    @OnlyIn(Dist.CLIENT)
    protected float passengerPitchOffset;

    public VehicleEntity(EntityType<?> entityType, Level worldIn)
    {
        super(entityType, worldIn);
        this.seatTracker = new SeatTracker(this);
        this.cosmeticTracker = new CosmeticTracker(this);
    }

    @Override
    protected void defineSynchedData()
    {
        this.entityData.define(TIME_SINCE_HIT, 0);
        this.entityData.define(HEALTH, 100F);
        this.entityData.define(COLOR, 16383998);
        this.entityData.define(TRAILER, -1);
        this.entityData.define(WHEEL_STACK, ItemStack.EMPTY);
    }

    public void registerDataValue(VehicleDataValue<?> dataValue)
    {
        this.paramToDataValue.put(dataValue.getKey(), dataValue);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key)
    {
        super.onSyncedDataUpdated(key);
        // Yeah pretty cool java stuff
        Optional.ofNullable(this.getControllingPassenger())
                .filter(entity -> entity instanceof Player && !((Player) entity).isLocalPlayer())
                .flatMap(entity -> Optional.ofNullable(this.paramToDataValue.get(key)))
                .ifPresent(value -> value.updateLocal(this));
    }

    /* Overridden to prevent odd step sound when driving vehicles. Ain't no subclasses getting
     * the ability to override this. */
    @Override
    protected final void playStepSound(BlockPos pos, BlockState blockIn) {}

    @Override
    public AABB getBoundingBoxForCulling()
    {
        return this.getBoundingBox().inflate(1);
    }

    @Override
    public boolean isPickable()
    {
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand)
    {
        if(!this.level().isClientSide() && !player.isCrouching())
        {
            int trailerId = SyncedPlayerData.instance().get(player, ModDataKeys.TRAILER);
            if(trailerId != -1)
            {
                if(this.getVehicle() == null && this.canTowTrailers() && this.getTrailer() == null)
                {
                    Entity entity = this.level().getEntity(trailerId);
                    if(entity instanceof TrailerEntity && entity != this)
                    {
                        TrailerEntity trailer = (TrailerEntity) entity;
                        this.setTrailer(trailer);
                        SyncedPlayerData.instance().set(player, ModDataKeys.TRAILER, -1);
                    }
                }
                return InteractionResult.SUCCESS;
            }

            ItemStack heldItem = player.getItemInHand(hand);
            if(heldItem.getItem() instanceof SprayCanItem)
            {
                if(this.getProperties().canBePainted())
                {
                    CompoundTag compound = heldItem.getTag();
                    if(compound != null)
                    {
                        if(!compound.contains("RemainingSprays", CompoundTag.TAG_INT))
                        {
                            compound.putInt("RemainingSprays", ModItems.SPRAY_CAN.get().getCapacity(heldItem));
                        }
                        int remainingSprays = compound.getInt("RemainingSprays");
                        if(compound.contains("Color", CompoundTag.TAG_INT) && remainingSprays > 0)
                        {
                            int color = compound.getInt("Color");
                            if(this.getColor() != color)
                            {
                                this.setColor(compound.getInt("Color"));
                                player.level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.ITEM_SPRAY_CAN_SPRAY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                                compound.putInt("RemainingSprays", remainingSprays - 1);
                            }
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            }
            else if(heldItem.getItem() == ModItems.HAMMER.get() && this.getVehicle() instanceof EntityJack)
            {
                if(this.getHealth() < this.getMaxHealth())
                {
                    heldItem.hurtAndBreak(1, player, Player -> player.broadcastBreakEvent(hand));
                    this.setHealth(this.getHealth() + 5F);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.ENTITY_VEHICLE_THUD.get(), SoundSource.PLAYERS, 1.0F, 0.8F + 0.4F * random.nextFloat());
                    player.swing(hand);
                    if(player instanceof ServerPlayer)
                    {
                        ((ServerPlayer) player).connection.send(new ClientboundAnimatePacket(player, hand == InteractionHand.MAIN_HAND ? 0 : 3));
                    }
                    if(this.getHealth() == this.getMaxHealth())
                    {
                        if(level() instanceof ServerLevel)
                        {
                            //TODO send as single packet instead of multiple
                            int count = (int) (50 * (this.getBbWidth() * this.getBbHeight()));
                            for(int i = 0; i < count; i++)
                            {
                                double width = this.getBbWidth() * 2;
                                double height = this.getBbHeight() * 1.5;

                                Vec3 heldOffset = this.getProperties().getHeldOffset().yRot((float) Math.toRadians(-this.yRot));
                                double x = this.getX() + width * random.nextFloat() - width / 2 + heldOffset.z * 0.0625;
                                double y = this.getY() + height * random.nextFloat();
                                double z = this.getZ() + width * random.nextFloat() - width / 2 + heldOffset.x * 0.0625;

                                double d0 = random.nextGaussian() * 0.02D;
                                double d1 = random.nextGaussian() * 0.02D;
                                double d2 = random.nextGaussian() * 0.02D;
                                ((ServerLevel) this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 1, d0, d1, d2, 1.0);
                            }
                        }
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.5F);
                    }
                }
                return InteractionResult.SUCCESS;
            }
            else if(this.canRide(player))
            {
                int seatIndex = this.seatTracker.getClosestAvailableSeatToPlayer(player);
                if(seatIndex != -1)
                {
                    if(player.startRiding(this))
                    {
                        this.getSeatTracker().setSeatIndex(seatIndex, player.getUUID());
                        this.onPlayerChangeSeat(player, -1, seatIndex);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }
    
    

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        if(compound.contains("Color", CompoundTag.TAG_INT_ARRAY))
        {
            int[] c = compound.getIntArray("Color");
            if(c.length == 3)
            {
                int color = ((c[0] & 0xFF) << 16) | ((c[1] & 0xFF) << 8) | ((c[2] & 0xFF));
                this.setColor(color);
            }
        }
        if(compound.contains("Health", CompoundTag.TAG_FLOAT))
        {
            this.setHealth(compound.getFloat("Health"));
        }
        if(compound.hasUUID("Trailer"))
        {
            this.trailerId = compound.getUUID("Trailer");
        }
        if(compound.contains("SeatTracker", CompoundTag.TAG_COMPOUND))
        {
            this.seatTracker.read(compound.getCompound("SeatTracker"));
        }
        if(compound.contains("CosmeticTracker", CompoundTag.TAG_COMPOUND))
        {
            this.cosmeticTracker.read(compound.getCompound("CosmeticTracker"));
        }
        if(compound.contains("WheelStack", CompoundTag.TAG_COMPOUND))
        {
            this.setWheelStack(ItemStack.of(compound.getCompound("WheelStack")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        compound.putIntArray("Color", this.getColorRGB());
        compound.putFloat("MaxHealth", this.getMaxHealth());
        compound.putFloat("Health", this.getHealth());

        //TODO make it save the entity
        if(this.trailerId != null)
        {
            compound.putUUID("Trailer", this.trailerId);
        }

        compound.put("SeatTracker", this.seatTracker.write());
        compound.put("CosmeticTracker", this.cosmeticTracker.write());
        CommonUtils.writeItemStackToTag(compound, "WheelStack", this.getWheelStack());
    }

    @Override
    public void tick()
    {
        this.cosmeticTracker.tick(this);

        if(this.getTimeSinceHit() > 0)
        {
            this.setTimeSinceHit(this.getTimeSinceHit() - 1);
        }

        if(!this.level().isClientSide())
        {
            if(this.searchDelay <= 0)
            {
                if(this.trailer != null)
                {
                    /* Updates periodically to ensure the client knows the vehicle/trailer connection.
                     * There is often problems on loading worlds that it doesn't sync correctly, so this
                     * is the fix. */
                    this.entityData.set(TRAILER, trailer.getId());
                    this.trailer.getEntityData().set(TrailerEntity.PULLING_ENTITY, this.getId());
                    this.searchDelay = Config.SERVER.trailerSyncCooldown.get();
                }
                else
                {
                    this.findTrailer();
                }
            }
            else
            {
                this.searchDelay--;
            }
        }

        if(this.level().isClientSide)
        {
            int entityId = this.entityData.get(TRAILER);
            if(entityId != -1)
            {
                Entity entity = this.level().getEntity(this.entityData.get(TRAILER));
                if(entity instanceof TrailerEntity)
                {
                    this.trailer = (TrailerEntity) entity;
                    this.trailerId = trailer.getUUID();
                }
                else if(this.trailer != null)
                {
                    this.trailer = null;
                    this.trailerId = null;
                }
            }
            else if(this.trailer != null)
            {
                this.trailer = null;
                this.trailerId = null;
            }
        }

        if(!this.level().isClientSide && this.trailer != null && (!this.trailer.isAlive() || this.trailer.getPullingEntity() != this))
        {
            this.setTrailer(null);
        }

        super.tick();
        this.tickLerp();
        this.onUpdateVehicle();

        if(this.level().isClientSide())
        {
            this.prevBodyRotationPitch = this.bodyRotationPitch;
            this.prevBodyRotationYaw = this.bodyRotationYaw;
            this.prevBodyRotationRoll = this.bodyRotationRoll;
            this.updateBodyRotations();
            this.updateWheelRotations();
            while(this.bodyRotationYaw - this.prevBodyRotationYaw < -180F)
            {
                this.prevBodyRotationYaw -= 360F;
            }
            while(this.bodyRotationYaw - this.prevBodyRotationYaw >= 180F)
            {
                this.prevBodyRotationYaw += 360F;
            }
            while(this.bodyRotationRoll - this.prevBodyRotationRoll < -180F)
            {
                this.prevBodyRotationRoll -= 360F;
            }
            while(this.bodyRotationRoll - this.prevBodyRotationRoll >= 180F)
            {
                this.prevBodyRotationRoll += 360F;
            }
        }
    }

    private void findTrailer()
    {
        if(!this.level().isClientSide && this.trailerId != null && this.trailer == null)
        {
            ServerLevel server = (ServerLevel) this.level;
            Entity entity = server.getEntity(this.trailerId);
            if(entity instanceof TrailerEntity)
            {
                this.setTrailer((TrailerEntity) entity);
                return;
            }
            this.trailerId = null;
        }
    }

    protected abstract void onUpdateVehicle();

    @Override
    public boolean hurt(DamageSource source, float amount)
    {
        if(this.isInvulnerableTo(source))
        {
            return false;
        }
        else if(!this.level.isClientSide() && this.isAlive())
        {
            Entity trueSource = source.getEntity();
            if(trueSource != null && this.hasPassenger(trueSource))
            {
                return false;
            }
            else
            {
                if(Config.SERVER.vehicleDamage.get())
                {
                    this.setTimeSinceHit(10);
                    this.setHealth(this.getHealth() - amount);
                }
                boolean isCreativeMode = trueSource instanceof Player && ((Player) trueSource).isCreative();
                if(isCreativeMode || this.getHealth() < 0.0F)
                {
                    this.onVehicleDestroyed((LivingEntity) trueSource);
                    this.remove(RemovalReason.DISCARDED);
                }

                return true;
            }
        }
        else
        {
            return true;
        }
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        if(Config.SERVER.vehicleDamage.get() && !this.immuneToFallDamage() && pFallDistance >= 4F && this.getDeltaMovement().y() < -1.0F)
        {
            float damage = pFallDistance / 2F;
//            this.hurt(new DamageSource(), damage);
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.ENTITY_VEHICLE_IMPACT.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
        }
        return true;
    }

    protected void onVehicleDestroyed(LivingEntity entity)
    {
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.ENTITY_VEHICLE_DESTROYED.get(), SoundSource.AMBIENT, 1.0F, 0.5F);

        boolean isCreativeMode = entity instanceof Player && ((Player) entity).isCreative();
        if(!isCreativeMode && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
        {
            WorkstationRecipe recipe = WorkstationRecipes.getRecipe(this.getType(), this.level);
            if(recipe != null)
            {
                //TODO make vehicle inoperable instead of destroying
                /*List<ItemStack> materials = recipe.getMaterials();
                for(ItemStack stack : materials)
                {
                    ItemStack copy = stack.copy();
                    int shrink = copy.getCount() / 2;
                    if(shrink > 0)
                        copy.shrink(this.random.nextInt(shrink + 1));
                    InventoryUtil.spawnItemStack(this.level, this.getX(), this.getY(), this.getZ(), copy);
                }*/
            }
        }
    }

    public int getDestroyedStage()
    {
        return 10 - (int) Math.max(1.0F, (int) Math.ceil(10.0F * (this.getHealth() / this.getMaxHealth())));
    }

    /**
     * Smooths the rendering on servers
     */
    private void tickLerp()
    {
        if(this.isControlledByLocalInstance())
        {
            this.lerpSteps = 0;
            this.setPos(this.getX(), this.getY(), this.getZ());
        }

        if(this.lerpSteps > 0)
        {
            double d0 = this.getX() + (this.lerpX - this.getX()) / (double) this.lerpSteps;
            double d1 = this.getY() + (this.lerpY - this.getY()) / (double) this.lerpSteps;
            double d2 = this.getZ() + (this.lerpZ - this.getZ()) / (double) this.lerpSteps;
            double d3 = Mth.wrapDegrees(this.lerpYaw - (double) this.yRot);
            this.yRot = (float) ((double) this.yRot + d3 / (double) this.lerpSteps);
            this.xRot = (float) ((double) this.xRot + (this.lerpPitch - (double) this.xRot) / (double) this.lerpSteps);
            --this.lerpSteps;
            this.setPos(d0, d1, d2);
            this.setRot(this.yRot, this.xRot);
        }
    }


    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
    {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYaw = (double) yaw;
        this.lerpPitch = (double) pitch;
        this.lerpSteps = 10;
    }

    @Override
    protected boolean canRide(Entity entityIn)
    {
        return true;
    }

    @Override
    public void push(double x, double y, double z) {}

    /**
     * Sets the time to count down from since the last time entity was hit.
     */
    public void setTimeSinceHit(int timeSinceHit)
    {
        this.entityData.set(TIME_SINCE_HIT, timeSinceHit);
    }

    /**
     * Gets the time since the last hit.
     */
    public int getTimeSinceHit()
    {
        return this.entityData.get(TIME_SINCE_HIT);
    }

    /**
     * Gets the max health of the vehicle.
     */
    public final float getMaxHealth()
    {
        return this.getProperties().getMaxHealth();
    }

    /**
     * Sets the current health of the vehicle.
     */
    public void setHealth(float health)
    {
        this.entityData.set(HEALTH, Mth.clamp(health, 0F, this.getMaxHealth()));
    }

    /**
     * Gets the current health of the vehicle.
     */
    public float getHealth()
    {
        return this.entityData.get(HEALTH);
    }

    public boolean hasWheelStack()
    {
        return !this.getWheelStack().isEmpty();
    }

    public void setWheelStack(ItemStack wheels)
    {
        this.entityData.set(WHEEL_STACK, wheels);
    }

    public ItemStack getWheelStack()
    {
        return this.entityData.get(WHEEL_STACK);
    }

    public Optional<IWheelType> getWheelType()
    {
        return IWheelType.fromStack(this.entityData.get(WHEEL_STACK));
    }

    //TODO look into this and why its here. May have to send vanilla event to client
    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateHurt(float passengerYawOffset)
    {
        this.setTimeSinceHit(10);
    }

    public void setColor(int color)
    {
        if(this.getProperties().canBePainted())
        {
            this.entityData.set(COLOR, color);
        }
    }

    public void setColorRGB(int r, int g, int b)
    {
        int color = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF));
        this.entityData.set(COLOR, color);
    }

    public int getColor()
    {
        return this.entityData.get(COLOR);
    }

    public int[] getColorRGB()
    {
        int color = this.entityData.get(COLOR);
        return new int[]{ (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF };
    }

    /**
     * Gets the absolute position of a part in the world
     *
     * @param position the position definition of the part
     * @return a Vector3d containing the exact location
     */
    public Vec3 getWorldPosition(Transform position, float partialTicks)
    {
        VehicleProperties properties = this.getProperties();
        Transform bodyPosition = properties.getBodyTransform();
        Vec3 partVec = Vec3.ZERO;
        partVec = partVec.add(0, 0.5, 0);
        partVec = partVec.scale(position.getScale());
        partVec = partVec.add(0, -0.5, 0);
        partVec = partVec.add(position.getX() * 0.0625, position.getY() * 0.0625, position.getZ() * 0.0625);
        partVec = partVec.add(0, properties.getWheelOffset() * 0.0625, 0);
        partVec = partVec.add(0, properties.getAxleOffset() * 0.0625, 0);
        partVec = partVec.add(0, 0.5, 0);
        partVec = partVec.scale(bodyPosition.getScale());
        partVec = partVec.add(0, -0.5, 0);
        partVec = partVec.add(0, 0.5, 0);
        partVec = partVec.add(bodyPosition.getX(), bodyPosition.getY(), bodyPosition.getZ());
        partVec = partVec.yRot(-(this.yRotO + (this.yRot - this.yRotO) * partialTicks) * 0.017453292F);
        partVec = partVec.add(this.xo + (this.getX() - this.xo) * partialTicks, 0, 0);
        partVec = partVec.add(0, this.yo + (this.getY() - this.yo) * partialTicks, 0);
        partVec = partVec.add(0, 0, this.zo + (this.getZ() - this.zo) * partialTicks);
        return partVec;
    }

    protected static AABB createScaledBoundingBox(double x1, double y1, double z1, double x2, double y2, double z2, double scale)
    {
        return new AABB(x1 * scale, y1 * scale, z1 * scale, x2 * scale, y2 * scale, z2 * scale);
    }

    protected static AABB createBoxScaled(double x1, double y1, double z1, double x2, double y2, double z2, double scale)
    {
        return new AABB(x1 * 0.0625 * scale, y1 * 0.0625 * scale, z1 * 0.0625 * scale, x2 * 0.0625 * scale, y2 * 0.0625 * scale, z2 * 0.0625 * scale);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {
        buffer.writeFloat(this.yRot);
        this.seatTracker.write(buffer);
        this.cosmeticTracker.write(buffer);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer)
    {
        this.yRot = this.yRotO = buffer.readFloat();
        this.seatTracker.read(buffer);
        this.cosmeticTracker.read(buffer);
    }

    public final boolean canTowTrailers()
    {
        return this.getProperties().canTowTrailers();
    }

    public void setTrailer(TrailerEntity trailer)
    {
        if(trailer != null)
        {
            this.trailer = trailer;
            this.trailerId = trailer.getUUID();
            trailer.setPullingEntity(this);
            this.entityData.set(TRAILER, trailer.getId());
        }
        else
        {
            if(this.trailer != null && this.trailer.getPullingEntity() == this)
            {
                this.trailer.setPullingEntity(null);
            }
            this.trailer = null;
            this.trailerId = null;
            this.entityData.set(TRAILER, -1);
        }
    }

    @Nullable
    public UUID getTrailerId()
    {
        return trailerId;
    }

    @Nullable
    public TrailerEntity getTrailer()
    {
        return trailer;
    }

    public final boolean canChangeWheels()
    {
        return this.getProperties().canChangeWheels();
    }

    public final boolean immuneToFallDamage()
    {
        return this.getProperties().immuneToFallDamage();
    }

    public final boolean canPlayerCarry()
    {
        return this.getProperties().canPlayerCarry();
    }

    public final boolean canFitInTrailer()
    {
        return this.getProperties().canFitInTrailer();
    }

    public VehicleProperties getProperties()
    {
        return VehicleProperties.get(this.getType());
    }



    @Override
    public ItemStack getPickedResult(HitResult target) {
        ResourceLocation entityId = new ResourceLocation(this.getType().getDescriptionId());
        if(entityId != null)
        {
            ItemStack wheel = ItemStack.EMPTY;
            if(this.hasWheelStack())
            {
                wheel = this.getWheelStack();
            }
            return VehicleCrateBlock.create(entityId, this.getColor(), null, wheel);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    

    public CosmeticTracker getCosmeticTracker()
    {
        return this.cosmeticTracker;
    }

    public SeatTracker getSeatTracker()
    {
        return this.seatTracker;
    }

    /**
     * Called when the player mounts a seat, changes seat, and dismounts a seat. If the oldSeatIndex
     * is -1 then the player is mounting the vehicle. If the newSeatIndex is -1 then the player is
     * dismounting the vehicle.
     * @param player the player changing seat
     * @param oldSeatIndex the index of the seat the player was previously sitting on
     * @param newSeatIndex the index of the seat the player is now sitting on
     */
    public void onPlayerChangeSeat(Player player, int oldSeatIndex, int newSeatIndex)
    {
        if(newSeatIndex != -1 && this.level.isClientSide())
        {
            Seat seat = this.getProperties().getSeats().get(newSeatIndex);
            player.yRot = this.yRot + seat.getYawOffset();
            player.setYHeadRot(player.yRot);
            this.updatePassengerOffsets(player);
            this.updatePassengerPosition(player);
        }
    }

    @Override
    protected void removePassenger(Entity passenger)
    {
        super.removePassenger(passenger);
        if(!this.level.isClientSide() && passenger instanceof Player)
        {
            int oldSeatIndex = this.seatTracker.getSeatIndex(passenger.getUUID());
            this.onPlayerChangeSeat((Player) passenger, oldSeatIndex, -1);
        }
    }

    @Override
    public void addPassenger(Entity passenger)
    {
        super.addPassenger(passenger);
        if(this.isControlledByLocalInstance() && this.lerpSteps > 0)
        {
            this.lerpSteps = 0;
            this.setPos(this.lerpX, this.lerpY, this.lerpZ);
            this.yRot = (float) this.lerpYaw;
            this.xRot = (float) this.lerpPitch;
        }

        // Makes the player face the same direction of the vehicle
        passenger.xRot = this.xRot;
        passenger.yRot = this.yRot;

        // Resets the passenger yaw offset
        if(passenger instanceof Player && ((Player) passenger).isLocalPlayer())
        {
            this.passengerYawOffset = 0;
            this.passengerPitchOffset = 0;
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger)
    {
        return this.getPassengers().size() < this.getProperties().getSeats().size();
    }

    @Override
    protected void positionRider(Entity pPassenger, MoveFunction pCallback) {
        super.positionRider(pPassenger, pCallback);
        this.updatePassengerPosition(pPassenger);
    }

    protected void updatePassengerPosition(Entity passenger)
    {
        if(this.hasPassenger(passenger))
        {
            int seatIndex = this.getSeatTracker().getSeatIndex(passenger.getUUID());
            if(seatIndex != -1)
            {
                VehicleProperties properties = this.getProperties();
                if(seatIndex >= 0 && seatIndex < properties.getSeats().size())
                {

                    Seat seat = properties.getSeats().get(seatIndex);
                    Vec3 seatVec = seat.getPosition().add(0, properties.getAxleOffset() + properties.getWheelOffset(), 0).scale(properties.getBodyTransform().getScale()).multiply(-1, 1, 1).add(properties.getBodyTransform().getTranslate()).scale(0.0625).yRot(-(this.yRot + 180) * 0.017453292F);
                    passenger.setPos(this.getX() - seatVec.x, this.getY() + seatVec.y + passenger.getMyRidingOffset(), this.getZ() - seatVec.z);
                    if(this.level.isClientSide() && VehicleHelper.canFollowVehicleOrientation(passenger))
                    {
                        //TODO launch the game to test this
                        if(Config.CLIENT.immersiveCamera.get() && Config.CLIENT.shouldFollowPitch.get())
                        {
                            passenger.xRotO = passenger.xRot;
                            passenger.xRot = this.xRot + this.passengerPitchOffset;
                        }
                        if(this.canApplyYawOffset(passenger) && Config.CLIENT.shouldFollowYaw.get())
                        {
                            passenger.yRot -= Mth.degreesDifference(this.yRot - this.passengerYawOffset, passenger.yRot);
                            passenger.setYHeadRot(passenger.yRot);
                        }
                    }
                    this.clampYaw(passenger);
                }
            }
        }
    }

    public boolean canApplyYawOffset(Entity passenger)
    {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    protected void clampYaw(Entity passenger)
    {
        int seatIndex = this.getSeatTracker().getSeatIndex(passenger.getUUID());
        float seatYawOffset = seatIndex != -1 ? this.getProperties().getSeats().get(seatIndex).getYawOffset() : 0F;
        passenger.setYBodyRot(this.yRot + seatYawOffset);
        float wrappedYaw = Mth.wrapDegrees(passenger.yRot - this.yRot - seatYawOffset);
        float clampedYaw = Mth.clamp(wrappedYaw, -120.0F, 120.0F);
        passenger.yRotO += clampedYaw - wrappedYaw;
        passenger.yRot += clampedYaw - wrappedYaw;
        passenger.setYHeadRot(passenger.yRot);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPassengerTurned(Entity passenger)
    {
        this.clampYaw(passenger);
        if(VehicleHelper.canFollowVehicleOrientation(passenger))
        {
            this.updatePassengerOffsets(passenger);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void updatePassengerOffsets(Entity passenger)
    {
        int seatIndex = this.getSeatTracker().getSeatIndex(passenger.getUUID());
        float seatYawOffset = seatIndex != -1 ? this.getProperties().getSeats().get(seatIndex).getYawOffset() : 0F;
        Vec3 vehicleForward = Vec3.directionFromRotation(new Vec2(0, this.yRot));
        Vec3 passengerForward = Vec3.directionFromRotation(new Vec2(passenger.xRot, passenger.getYHeadRot()));
        this.passengerPitchOffset = Mth.degreesDifference(CommonUtils.pitch(passengerForward), CommonUtils.pitch(vehicleForward)) - this.xRot;

        if(!this.canApplyYawOffset(passenger))
        {
            this.passengerYawOffset = 0;
        }
        else
        {
            this.passengerYawOffset = Mth.degreesDifference(Mth.wrapDegrees(passenger.yRot) + seatYawOffset, CommonUtils.yaw(vehicleForward));
            this.passengerYawOffset += seatYawOffset;
        }
    }

    protected void updateBodyRotations()
    {
        this.bodyRotationYaw = this.yRot;
    }

    @OnlyIn(Dist.CLIENT)
    public float getBodyRotationPitch(float partialTicks)
    {
        return Mth.lerp(partialTicks, this.prevBodyRotationPitch, this.bodyRotationPitch);
    }

    @OnlyIn(Dist.CLIENT)
    public float getBodyRotationYaw(float partialTicks)
    {
        return Mth.rotLerp(partialTicks, this.prevBodyRotationYaw, this.bodyRotationYaw);
    }

    @OnlyIn(Dist.CLIENT)
    public float getBodyRotationRoll(float partialTicks)
    {
        return Mth.rotLerp(partialTicks, this.prevBodyRotationRoll, this.bodyRotationRoll);
    }

    @OnlyIn(Dist.CLIENT)
    public float getViewPitch(float partialTicks)
    {
        return this.getBodyRotationPitch(partialTicks);
    }

    @OnlyIn(Dist.CLIENT)
    public float getViewYaw(float partialTicks)
    {
        return this.getBodyRotationYaw(partialTicks);
    }

    @OnlyIn(Dist.CLIENT)
    public float getViewRoll(float partialTicks)
    {
        return this.getBodyRotationRoll(partialTicks);
    }

    @OnlyIn(Dist.CLIENT)
    public float getPassengerYawOffset()
    {
        return this.passengerYawOffset;
    }

    @OnlyIn(Dist.CLIENT)
    public float getPassengerPitchOffset()
    {
        return this.passengerPitchOffset;
    }

    @OnlyIn(Dist.CLIENT)
    protected void updateWheelRotations() {}

    @OnlyIn(Dist.CLIENT)
    public float getWheelRotation(@Nullable Wheel wheel, float partialTicks)
    {
        return 0F;
    }
}