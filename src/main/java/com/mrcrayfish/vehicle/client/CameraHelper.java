package com.mrcrayfish.vehicle.client;

import com.mojang.math.Axis;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.util.MathUtil;
import com.mrcrayfish.vehicle.common.Seat;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.TransformationHelper;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.joml.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A helper class that manages the camera rotations for vehicles
 *
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class CameraHelper
{
    private static final Method SET_POSITION_METHOD = ObfuscationReflectionHelper.findMethod(CameraType.class, "func_216775_b", double.class, double.class, double.class);
    private static final Method MOVE_METHOD = ObfuscationReflectionHelper.findMethod(CameraType.class, "func_216782_a", double.class, double.class, double.class);
    private static final Method GET_MAX_MOVE_METHOD = ObfuscationReflectionHelper.findMethod(CameraType.class, "func_216779_a", double.class);
    private static final Field LEFT_FIELD = ObfuscationReflectionHelper.findField(CameraType.class, "field_216796_h");

    private VehicleProperties properties;
    private Quaternionf currentRotation;
    private Quaternionf prevRotation;
    private float pitchOffset;
    private float yawOffset;

    // Debug properties
    public float debugOffsetX;
    public float debugOffsetY;
    public float debugOffsetZ;
    public float debugOffsetPitch;
    public float debugOffsetYaw;
    public float debugOffsetRoll;
    public boolean debugEnableStrength = true;

    public void load(VehicleEntity vehicle)
    {
        this.properties = vehicle.getProperties();
        this.pitchOffset = 0F;
        this.yawOffset = 0F;
        this.currentRotation = new Quaternionf(0.0F,((double) vehicle.getViewPitch(1F)), ((double) - vehicle.getViewYaw(1F)), ((double) vehicle.getViewRoll(1F)));
        this.prevRotation = new Quaternionf(this.currentRotation);
    }

    public void tick(VehicleEntity vehicle, CameraType pov)
    {
        float strength = this.getStrength(pov);
        this.prevRotation = this.currentRotation;
        Quaternionf quaternion = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
        quaternion.mul(Axis.YP.rotationDegrees(-vehicle.getViewYaw(1F) + (Config.CLIENT.debugCamera.get() ? this.debugOffsetYaw : 0F)));
        quaternion.mul(Axis.XP.rotationDegrees(vehicle.getViewPitch(1F) + (Config.CLIENT.debugCamera.get() ? this.debugOffsetPitch : 0F)));
        quaternion.mul(Axis.ZP.rotationDegrees(vehicle.getViewRoll(1F) + (Config.CLIENT.debugCamera.get() ? this.debugOffsetRoll : 0F)));
        this.currentRotation = MathUtil.slerp(this.currentRotation, quaternion, strength);
    }

    private float getStrength(CameraType pov)
    {
        return (!Config.CLIENT.debugCamera.get() || this.debugEnableStrength) && pov == CameraType.THIRD_PERSON_BACK && this.properties.getCamera().getType() != CameraProperties.Type.LOCKED ? this.properties.getCamera().getStrength() : 1.0F;
    }

    public void setupVanillaCamera(Camera info, CameraType pov, VehicleEntity vehicle, AbstractClientPlayer player, float partialTicks)
    {
        switch(pov)
        {
            case FIRST_PERSON:
                this.setupFirstPersonCamera(info, vehicle, player, partialTicks);
                break;
            case THIRD_PERSON_BACK:
                this.setupThirdPersonCamera(info, vehicle, player, partialTicks, false);
                break;
            case THIRD_PERSON_FRONT:
                this.setupThirdPersonCamera(info, vehicle, player, partialTicks, true);
                break;
        }
    }

    private void setupFirstPersonCamera(Camera info, VehicleEntity vehicle, AbstractClientPlayer player, float partialTicks) {
        try {
            // Get the seat index for the player
            int index = vehicle.getSeatTracker().getSeatIndex(player.getUUID());
            if (index != -1) {
                // Apply vehicle orientation if enabled
                if (Config.CLIENT.followVehicleOrientation.get()) {
                    this.setVehicleRotation(info, vehicle, player, partialTicks);
                }

                // Retrieve seat properties
                Seat seat = this.properties.getSeats().get(index);

                // Calculate the eye position relative to the vehicle
                Vec3 seatPosition = seat.getPosition();
                double yOffset = this.properties.getAxleOffset() + this.properties.getWheelOffset();
                Vec3 eyePos = seatPosition.add(0, yOffset, 0)
                        .scale(this.properties.getBodyTransform().getScale())
                        .multiply(-1, 1, 1)
                        .add(this.properties.getBodyTransform().getTranslate())
                        .scale(0.0625);

                // Add player's riding offset and eye height
                eyePos = eyePos.add(0, player.getMyRidingOffset() + player.getEyeHeight(), 0);

                // Interpolate rotation using SLERP
                Quaterniondc interpolatedRotation = (Quaterniondc) MathUtil.slerp(this.prevRotation, this.currentRotation, partialTicks);
                AxisAngle4f aa4f = new AxisAngle4f((float) interpolatedRotation.angle(), (float) eyePos.x, (float) eyePos.y, (float) eyePos.z);
                Quaterniond rotatedEyePos = new Quaterniond(aa4f);
                rotatedEyePos.add(interpolatedRotation);

                // Calculate the final camera position
                double cameraX = Mth.lerp(partialTicks, vehicle.xo, vehicle.getX()) + rotatedEyePos.x();
                double cameraY = Mth.lerp(partialTicks, vehicle.yo, vehicle.getY()) + rotatedEyePos.y();
                double cameraZ = Mth.lerp(partialTicks, vehicle.zo, vehicle.getZ()) + rotatedEyePos.z();

                // Set the camera position
                SET_POSITION_METHOD.invoke(info, cameraX, cameraY, cameraZ);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    private void setupThirdPersonCamera(Camera info, VehicleEntity vehicle, AbstractClientPlayer player, float partialTicks, boolean front)
    {
        try
        {
            if(Config.CLIENT.followVehicleOrientation.get())
            {
                this.setVehicleRotation(info, vehicle, player, partialTicks);
            }

            if(Config.CLIENT.useVehicleAsFocusPoint.get() && !front)
            {
                Vec3 position = this.properties.getCamera().getPosition();
                Vector3f rotatedPosition = new Vector3f((float) position.x, (float) position.y, (float) position.z);
                if(Config.CLIENT.debugCamera.get()) rotatedPosition.add(this.debugOffsetX, this.debugOffsetY, this.debugOffsetZ);
                rotatedPosition.rotate(MathUtil.slerp(this.prevRotation, this.currentRotation, partialTicks));
                float cameraX = (float) (Mth.lerp(partialTicks, vehicle.xo, vehicle.getX()) + rotatedPosition.x());
                float cameraY = (float) (Mth.lerp(partialTicks, vehicle.yo, vehicle.getY()) + rotatedPosition.y());
                float cameraZ = (float) (Mth.lerp(partialTicks, vehicle.zo, vehicle.getZ()) + rotatedPosition.z());
                SET_POSITION_METHOD.invoke(info, cameraX, cameraY, cameraZ);
            }
            else
            {
                int index = vehicle.getSeatTracker().getSeatIndex(player.getUUID());
                if(index != -1)
                {
                    Seat seat = this.properties.getSeats().get(index);
                    Vec3 eyePos = seat.getPosition().add(0, this.properties.getAxleOffset() + this.properties.getWheelOffset(), 0).scale(this.properties.getBodyTransform().getScale()).multiply(-1, 1, 1).add(this.properties.getBodyTransform().getTranslate()).scale(0.0625);
                    eyePos = eyePos.add(0, player.getMyRidingOffset() + player.getEyeHeight(), 0);
                    Vector3f rotatedEyePos = new Vector3f((float) eyePos.x, (float) eyePos.y, (float) eyePos.z);
                    rotatedEyePos.rotate(TransformationHelper.slerp(this.prevRotation, this.currentRotation, partialTicks));
                    float cameraX = (float) (Mth.lerp(partialTicks, vehicle.xo, vehicle.getX()) + rotatedEyePos.x());
                    float cameraY = (float) (Mth.lerp(partialTicks, vehicle.yo, vehicle.getY()) + rotatedEyePos.y());
                    float cameraZ = (float) (Mth.lerp(partialTicks, vehicle.zo, vehicle.getZ()) + rotatedEyePos.z());
                    SET_POSITION_METHOD.invoke(info, cameraX, cameraY, cameraZ);
                }
            }

            double distance = front ? 4.0 : this.properties.getCamera().getDistance();
            MOVE_METHOD.invoke(info, -(double) GET_MAX_MOVE_METHOD.invoke(info, distance), 0, 0);
        }
        catch(InvocationTargetException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    private void setVehicleRotation(Camera info, VehicleEntity vehicle, AbstractClientPlayer player, float partialTicks)
    {
        try
        {
            Quaternionf rotation = info.rotation();
            rotation.set(0.0F, 0.0F, 0.0F, 1.0F);

            // Applies the vehicle's body rotations to the camera
            //TODO add this back
            /*if(Config.CLIENT.shouldFollowYaw.get())
            {
                rotation.mul(Vector3f.YP.rotationDegrees(-this.getYaw(partialTicks)));
            }
            if(Config.CLIENT.shouldFollowPitch.get())
            {
                rotation.mul(Vector3f.XP.rotationDegrees(this.getPitch(partialTicks)));
            }
            if(Config.CLIENT.shouldFollowRoll.get())
            {
                rotation.mul(Vector3f.ZP.rotationDegrees(this.getRoll(partialTicks)));
            }*/

            rotation.mul(MathUtil.slerp(this.prevRotation, this.currentRotation, partialTicks));

            // Applies the player's pitch and yaw offset
            Quaternionf quaternion = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);

            if(VehicleHelper.isThirdPersonFront())
            {
                quaternion.mul(Axis.YP.rotationDegrees(180F));
            }

            if(vehicle.canApplyYawOffset(player) && Config.CLIENT.shouldFollowYaw.get())
            {
                quaternion.mul(Axis.YP.rotationDegrees(-this.yawOffset));
            }
            else
            {
                quaternion.mul(Axis.YP.rotationDegrees(-player.getViewYRot(partialTicks)));
                if(Config.CLIENT.shouldFollowYaw.get())
                {
                    quaternion.mul(Axis.YP.rotationDegrees(vehicle.getViewYaw(partialTicks)));
                }
            }

            if(Config.CLIENT.shouldFollowPitch.get())
            {
                quaternion.mul(Axis.XP.rotationDegrees(VehicleHelper.isThirdPersonFront() ? -this.pitchOffset : this.pitchOffset));
            }
            else
            {
                quaternion.mul(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, player.xRotO, player.xRot)));
            }

            // If the player is in third person, applies additional vehicle specific camera rotations
            if(Config.CLIENT.useVehicleAsFocusPoint.get() && VehicleHelper.isThirdPersonBack())
            {
                CameraProperties camera = vehicle.getProperties().getCamera();
                Vec3 cameraRotation = camera.getRotation();
                quaternion.mul(Axis.YP.rotationDegrees((float) cameraRotation.y));
                quaternion.mul(Axis.XP.rotationDegrees((float) cameraRotation.x));
                quaternion.mul(Axis.ZP.rotationDegrees((float) cameraRotation.z));
            }

            // Finally applies local rotations to the camera
            rotation.mul(quaternion);

            Vector3f forward = info.getLookVector();
            forward.set(0.0F, 0.0F, 1.0F);
            forward.rotate(rotation);

            Vector3f up = info.getUpVector();
            up.set(0.0F, 1.0F, 0.0F);
            up.rotate(rotation);

            Vector3f left = (Vector3f) LEFT_FIELD.get(info);
            left.set(1.0F, 0.0F, 0.0F);
            left.rotate(rotation);
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    public void turnPlayerView(double x, double y)
    {
        this.pitchOffset += y * 0.15F;
        this.yawOffset += x * 0.15F;
        this.pitchOffset = Mth.clamp(this.pitchOffset, -90F, 90F);
        this.yawOffset = Mth.clamp(this.yawOffset, -120F, 120F);
    }
}
