package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.entity.HelicopterEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.init.ModParticleTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import net.minecraftforge.common.Tags;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Author: MrCrayfish
 */
public class CompactHelicopterEntity extends HelicopterEntity
{
    public CompactHelicopterEntity(EntityType<?> entityType, Level worldIn)
    {
        super(entityType, worldIn);
    }

    @Override
    public void onClientUpdate()
    {
        super.onClientUpdate();

        if (this.canDrive() && this.tickCount % 2 == 0)
        {
            Vec3 exhaust = this.getExhaustFumesPosition().scale(0.0625);
            Vector4f fumePosition = new Vector4f((float) exhaust.x, (float) exhaust.y, (float) exhaust.z, 1.0F);
            fumePosition.mul(this.getTransformMatrix(0F));
            this.level.addParticle(ParticleTypes.LARGE_SMOKE, this.getX() + fumePosition.x(), this.getY() + fumePosition.y(), this.getZ() + fumePosition.z(), -this.getDeltaMovement().x(), 0.0D, -this.getDeltaMovement().z());
        }

        if (this.bladeSpeed > 30.0F)
        {
            double bladeScale = this.bladeSpeed * 0.001;
            double spreadRange = 8.0;
            double randX = -(spreadRange / 2.0) + spreadRange * this.random.nextDouble();
            double randZ = -(spreadRange / 2.0) + spreadRange * this.random.nextDouble();
            double posX = this.getX() + randX;
            double posZ = this.getZ() + randZ;
            double downDistance = Math.min(12.0, this.bladeSpeed / 15.0);
            downDistance = (downDistance * 0.5) + (downDistance * 0.5) * this.random.nextDouble();
            Vec3 start = new Vec3(posX, this.getY() + 3.0, posZ);
            Vec3 end = start.subtract(0, downDistance, 0);
            BlockHitResult result = this.level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));
            if (result.getType() != HitResult.Type.MISS)
            {
                Vec3 loc = result.getLocation();
                double distanceScale = (downDistance - start.distanceTo(loc)) / downDistance;
                BlockState state = this.level.getBlockState(result.getBlockPos());
                if (state.is(Tags.Blocks.GRAVEL) || state.is(Tags.Blocks.GRAVEL) || state.is(Tags.Blocks.SAND))
                {
                    this.level.addParticle(ModParticleTypes.DUST.get(), loc.x(), loc.y(), loc.z(), randX * bladeScale * distanceScale, 0.02, randZ * bladeScale * distanceScale);
                }
                else if (state.getFluidState().is(FluidTags.WATER))
                {
                    this.level.addParticle(ParticleTypes.SPLASH, loc.x(), loc.y(), loc.z(), randX * bladeScale * distanceScale, 0.02, randZ * bladeScale * distanceScale);
                    this.level.addParticle(ParticleTypes.BUBBLE, loc.x(), loc.y(), loc.z(), randX * bladeScale * distanceScale, 0.02, randZ * bladeScale * distanceScale);
                    this.level.addParticle(ParticleTypes.CLOUD, loc.x(), loc.y(), loc.z(), 0, 0, 0);
                }
            }
        }
    }

    // Client only TODO move to base vehicle class
    private Matrix4f getTransformMatrix(float partialTicks)
    {
        Matrix4f matrix = new Matrix4f().identity();

        // Apply rotation transformations
        matrix.rotateY((float) Math.toRadians(-this.getBodyRotationYaw(partialTicks)));
        matrix.rotateX((float) Math.toRadians(this.getBodyRotationPitch(partialTicks)));
        matrix.rotateZ((float) Math.toRadians(this.getBodyRotationRoll(partialTicks)));

        // Apply scaling transformation
        VehicleProperties properties = this.getProperties();
        Transform bodyPosition = properties.getBodyTransform();
        matrix.scale((float) bodyPosition.getScale());

        // Apply translation transformations
        Vector3f translate = new Vector3f(
                (float) bodyPosition.getX() * 0.0625F,
                (float) bodyPosition.getY() * 0.0625F,
                (float) bodyPosition.getZ() * 0.0625F
        );
        translate.add(0.0F, 0.5F + properties.getAxleOffset() * 0.0625F + properties.getWheelOffset() * 0.0625F, 0.0F);
        matrix.translate(translate);

        return matrix;
    }
}