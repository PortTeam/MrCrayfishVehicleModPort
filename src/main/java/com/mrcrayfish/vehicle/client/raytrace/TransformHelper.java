package com.mrcrayfish.vehicle.client.raytrace;

import com.google.common.collect.Lists;
import com.mrcrayfish.vehicle.client.model.ComponentModel;
import com.mrcrayfish.vehicle.client.model.IComplexModel;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.data.ItemStackRayTraceData;
import com.mrcrayfish.vehicle.client.raytrace.data.RayTraceData;
import com.mrcrayfish.vehicle.client.raytrace.data.ComponentModelRayTraceData;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class TransformHelper
{
    /**
     * Creates a body transformation based on a PartPosition for a raytraceable entity's body. These
     * arguments should be the same as the static properties defined for the vehicle.
     *
     * @param transforms the global transformation matrix
     * @param entityType the vehicle entity type
     */
    public static void createBodyTransforms(List<Matrix4f> transforms, EntityType<? extends VehicleEntity> entityType)
    {
        VehicleProperties properties = VehicleProperties.get(entityType);
        Transform bodyPosition = properties.getBodyTransform();
        Matrix4f matrix4f = new Matrix4f();
        transforms.add(matrix4f.scale((float) bodyPosition.getScale()));
        transforms.add(matrix4f.translate((float) bodyPosition.getX() * 0.0625F, (float) bodyPosition.getY() * 0.0625F, (float) bodyPosition.getZ() * 0.0625F));
        transforms.add(matrix4f.translate(0.0F, 0.5F, 0.0F));
        transforms.add(matrix4f.translate(0.0F, properties.getAxleOffset() * 0.0625F, 0.0F));
        transforms.add(matrix4f.translate(0.0F, properties.getWheelOffset() * 0.0625F, 0.0F));
        transforms.add(matrix4f.rotate(Axis.POSITIVE_X.rotationDegrees((float) bodyPosition.getRotX())));
        transforms.add(matrix4f.rotate(Axis.POSITIVE_Y.rotationDegrees((float) bodyPosition.getRotY())));
        transforms.add(matrix4f.rotate(Axis.POSITIVE_Z.rotationDegrees((float) bodyPosition.getRotZ())));
        transforms.add(matrix4f.translate(0.5F, 0.5F, 0.5F));
    }

    public static void createSimpleTransforms(List<Matrix4f> transforms, Transform transform)
    {
        Matrix4f matrix4f = new Matrix4f();
        transforms.add(matrix4f.scale((float) transform.getScale()));
        transforms.add(matrix4f.translate((float) transform.getX() * 0.0625F, (float) transform.getY() * 0.0625F, (float) transform.getZ() * 0.0625F));
        transforms.add(matrix4f.rotate(Axis.POSITIVE_X.rotationDegrees((float) transform.getRotX())));
        transforms.add(matrix4f.rotate(Axis.POSITIVE_Y.rotationDegrees((float) transform.getRotY())));
        transforms.add(matrix4f.rotate(Axis.POSITIVE_Z.rotationDegrees((float) transform.getRotZ())));
    }

    public static void createPartTransforms(ComponentModel model, Transform transform, HashMap<RayTraceData, List<Matrix4f>> parts, List<Matrix4f> globalTransforms, @Nullable RayTraceFunction function)
    {
        createPartTransforms(new ComponentModelRayTraceData(model, function), transform.getTranslate(), transform.getRotation(), (float) transform.getScale(), parts, globalTransforms);
    }

    public static void createPartTransforms(Item part, Transform transform, HashMap<RayTraceData, List<Matrix4f>> parts, List<Matrix4f> globalTransforms, @Nullable RayTraceFunction function)
    {
        createPartTransforms(new ItemStackRayTraceData(new ItemStack(part), function), transform.getTranslate(), transform.getRotation(), (float) transform.getScale(), parts, globalTransforms);
    }

    public static void createPartTransforms(RayTraceData data, Vec3 offset, Vec3 rotation, float scale, HashMap<RayTraceData, List<Matrix4f>> parts, List<Matrix4f> transformsGlobal)
    {
        List<Matrix4f> transforms = Lists.newArrayList();
        Matrix4f matrix4f  = new Matrix4f();
        transforms.addAll(transformsGlobal);
        transforms.add(matrix4f.translate((float) offset.x * 0.0625F, (float) offset.y * 0.0625F, (float) offset.z * 0.0625F));
        transforms.add(matrix4f.translate(0.0F, -0.5F, 0.0F));
        transforms.add(matrix4f.scale(scale));
        transforms.add(matrix4f.rotate(Axis.POSITIVE_X.rotationDegrees((float) rotation.x)));
        transforms.add(matrix4f.rotate(Axis.POSITIVE_Y.rotationDegrees((float) rotation.y)));
        transforms.add(matrix4f.rotate(Axis.POSITIVE_Z.rotationDegrees((float) rotation.z)));
        createTransformListForPart((ItemStackRayTraceData) data, parts, transforms);
    }

    public static void createTransformListForPart(ComponentModel model, HashMap<RayTraceData, List<Matrix4f>> parts, List<Matrix4f> globalTransforms, Matrix4f... transforms)
    {
        createTransformListForPart(new ComponentModelRayTraceData(model).getModel(), parts, globalTransforms, transforms);
    }

    public static void createTransformListForPart(ComponentModel model, HashMap<RayTraceData, List<Matrix4f>> parts, List<Matrix4f> globalTransforms, @Nullable RayTraceFunction function, Matrix4f... transforms)
    {
        createTransformListForPart(new ComponentModelRayTraceData(model, function).getModel(), parts, globalTransforms, transforms);
    }

    public static void createTransformListForPart(Item item, HashMap<RayTraceData, List<Matrix4f>> parts, List<Matrix4f> globalTransforms, Matrix4f... transforms)
    {
        createTransformListForPart(new ItemStackRayTraceData(new ItemStack(item)), parts, globalTransforms, transforms);
    }



    public static void createTransformListForPart(Item item, HashMap<RayTraceData, List<Matrix4f>> parts, List<Matrix4f> globalTransforms, @Nullable RayTraceFunction function, Matrix4f... transforms)
    {
        createTransformListForPart(new ItemStackRayTraceData(new ItemStack(item), function), parts, globalTransforms, transforms);
    }


    public static void createTransformListForPart(ItemStackRayTraceData data, HashMap<RayTraceData, List<Matrix4f>> parts, List<Matrix4f> globalTransforms, Matrix4f... transforms)
    {
        List<Matrix4f> transformsAll = Lists.newArrayList();
        transformsAll.addAll(globalTransforms);
        transformsAll.addAll(Arrays.asList(transforms));
        parts.put(data, transformsAll);
        data.clearTriangles();
        data.setMatrix(createMatrixFromTransformsForPart(transformsAll));
    }

    public static void createEngineTransforms(Item engineItem, EntityType<? extends VehicleEntity> entityType, HashMap<RayTraceData, List<Matrix4f>> parts, List<Matrix4f> globalTransforms, @Nullable RayTraceFunction function)
    {
        Transform engineTransform = VehicleProperties.get(entityType).getExtended(PoweredProperties.class).getEngineTransform();
        List<Matrix4f> transforms = new ArrayList<>(globalTransforms);
        Matrix4f matrix4f = new Matrix4f();
        transforms.add(matrix4f.translate(0.0F, 0.5F * (float) engineTransform.getScale(), 0.0F));
        createPartTransforms(engineItem, engineTransform, parts, transforms, function);
    }

    /**
     * Creates part-specific transforms for a raytraceable entity's rendered part and adds them the list of transforms
     * for the given entity.
     *
     * @param entityType the vehicle entity type
     * @param parts map of all parts to their transforms
     */
    public static void createTowBarTransforms(EntityType<? extends VehicleEntity> entityType, ComponentModel model, HashMap<RayTraceData, List<Matrix4f>> parts)
    {
        VehicleProperties properties = VehicleProperties.get(entityType);
        double bodyScale = properties.getBodyTransform().getScale();
        List<Matrix4f> transforms = new ArrayList<>();
        Matrix4f matrix4f = new Matrix4f();
        transforms.add(matrix4f.rotate(com.mojang.math.Axis.YP.rotationDegrees(180F)));
        transforms.add(matrix4f.translate(0.0F, 0.5F, 0.0F));
        transforms.add(matrix4f.translate(0.0F, 0.5F, 0.0F)); // Need extra translate to prevent translation in #createPartTransforms call
        Vec3 towBarOffset = properties.getTowBarOffset().scale(bodyScale).multiply(1, 1, -1);
        createPartTransforms(new ComponentModelRayTraceData(model), towBarOffset, Vec3.ZERO, 1.0F, parts, transforms);
    }

    /**
     * Creates part-specific transforms for a raytraceable entity's rendered part and adds them the list of transforms
     * for the given entity.
     *
     * @param entityType the vehicle entity type
     * @param parts map of all parts to their transforms
     * @param transformsGlobal transforms that construct to all parts for this entity
     */
    public static void createFuelFillerTransforms(EntityType<? extends VehicleEntity> entityType, ComponentModel model, HashMap<RayTraceData, List<Matrix4f>> parts, List<Matrix4f> transformsGlobal)
    {
        Transform fuelPortPosition = VehicleProperties.get(entityType).getExtended(PoweredProperties.class).getFuelFillerTransform();
        createPartTransforms(model, fuelPortPosition, parts, transformsGlobal, RayTraceFunction.FUNCTION_FUELING);
    }

    public static void createIgnitionTransforms(EntityType<? extends VehicleEntity> entityType, HashMap<RayTraceData, List<Matrix4f>> parts, List<Matrix4f> globalTransforms)
    {
        Transform ignitionTransform = VehicleProperties.get(entityType).getExtended(PoweredProperties.class).getIgnitionTransform();
        createPartTransforms(VehicleModels.KEY_HOLE, ignitionTransform, parts, globalTransforms, null);
    }

    public static Matrix4f createMatrixFromTransformsForPart(List<Matrix4f> transforms)
    {
        return createMatrixFromTransforms(transforms, -0.5F, -0.5F, -0.5F);
    }

    public static Matrix4f createMatrixFromTransformsForInteractionBox(List<Matrix4f> transforms)
    {
        return createMatrixFromTransforms(transforms, 0.0F, -0.5F, 0.0F);
    }

    public static Matrix4f createMatrixFromTransforms(List<Matrix4f> transforms, float xOffset, float yOffset, float zOffset)
    {
        Matrix4f matrix = new Matrix4f();

        Vector4f vector4f = new Vector4f(matrix.determinant());
        matrix.identity();
        transforms.forEach(t -> t.transform(vector4f));
        MatrixTransform.translate(xOffset, yOffset, zOffset).transform(matrix);
        return matrix;
    }
}
