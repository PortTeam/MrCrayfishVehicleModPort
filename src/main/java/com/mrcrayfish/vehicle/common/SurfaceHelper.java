package com.mrcrayfish.vehicle.common;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.vehicle.entity.IWheelType;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.entity.Wheel;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.minecraft.world.level.block.Blocks.*;


/**
 * Categories materials into a surface type to determine the
 * Author: MrCrayfish
 */
public class SurfaceHelper
{
    private static final ImmutableMap<Block, SurfaceType> MATERIAL_TO_SURFACE_TYPE;

    static
    {
        ImmutableMap.Builder<Block, SurfaceType> builder = new ImmutableMap.Builder<>();
        builder.put(CLAY, SurfaceType.DIRT);
        builder.put(DIRT, SurfaceType.DIRT);
        builder.put(GRASS, SurfaceType.DIRT);
        builder.put(ICE, SurfaceType.ICE);
        builder.put(SAND, SurfaceType.DIRT);
        builder.put(SPONGE, SurfaceType.DIRT);
        builder.put(SHULKER_BOX, SurfaceType.SOLID);
        builder.put(ACACIA_WOOD, SurfaceType.SOLID);
        builder.put(CRIMSON_STEM, SurfaceType.SOLID);
        builder.put(BAMBOO, SurfaceType.SOLID);
        builder.put(BLACK_WOOL, SurfaceType.DIRT);
        //builder.put(EXPLOSIVE, SurfaceType.SNOW);
        //builder.put(LEAVES, SurfaceType.SNOW);
        builder.put(GLASS, SurfaceType.SOLID);
        builder.put(ICE, SurfaceType.ICE);
        builder.put(CACTUS, SurfaceType.SNOW);
        builder.put(STONE, SurfaceType.SOLID);
        //builder.put(METAL, SurfaceType.SOLID);
        builder.put(SNOW, SurfaceType.SNOW);
        //builder.put(HEAVY_METAL, SurfaceType.SOLID);
        builder.put(BARRIER, SurfaceType.SOLID);
        builder.put(PISTON, SurfaceType.SOLID);
        //builder.put(CORAL, SurfaceType.SNOW);
        builder.put(CAKE, SurfaceType.SNOW);
        MATERIAL_TO_SURFACE_TYPE = builder.build();
    }

    public static SurfaceType getSurfaceTypeForMaterial(Block material)
    {
        return MATERIAL_TO_SURFACE_TYPE.getOrDefault(material, SurfaceType.NONE);
    }

    private static float getValue(PoweredVehicleEntity vehicle, BiFunction<IWheelType, SurfaceType, Float> function, float defaultValue)
    {
        VehicleProperties properties = vehicle.getProperties();
        List<Wheel> wheels = properties.getWheels();
        if(!vehicle.hasWheelStack() || wheels.isEmpty())
            return defaultValue;

        Optional<IWheelType> optional = vehicle.getWheelType();
        if(!optional.isPresent())
            return defaultValue;

        int wheelCount = 0;
        float surfaceModifier = 0F;
        double[] wheelPositions = vehicle.getWheelPositions();
        for(int i = 0; i < wheels.size(); i++)
        {
            double wheelX = wheelPositions[i * 3];
            double wheelY = wheelPositions[i * 3 + 1];
            double wheelZ = wheelPositions[i * 3 + 2];
            int x = Mth.floor(vehicle.getX() + wheelX);
            int y = Mth.floor(vehicle.getY() + wheelY - 0.2D);
            int z = Mth.floor(vehicle.getZ() + wheelZ);
            BlockState state = vehicle.level.getBlockState(new BlockPos(x, y, z));
            SurfaceType surfaceType = getSurfaceTypeForMaterial(state.getBlock());
            if(surfaceType == SurfaceType.NONE)
                continue;
            IWheelType wheelType = optional.get();
            surfaceModifier += function.apply(wheelType, surfaceType);
            wheelCount++;
        }
        return surfaceModifier / Math.max(1F, wheelCount);
    }

    public static float getFriction(PoweredVehicleEntity vehicle)
    {
        return getValue(vehicle, (wheelType, surfaceType) -> surfaceType.friction * surfaceType.wheelFunction.apply(wheelType), 0.0F);
    }

    public static float getSurfaceTraction(PoweredVehicleEntity vehicle, float original)
    {
        return getValue(vehicle, (wheelType, surfaceType) -> surfaceType.tractionFactor, 1.0F) * original;
    }

    public enum SurfaceType
    {
        SOLID(IWheelType::getRoadFrictionFactor, 0.9F, 1.0F),
        DIRT(IWheelType::getDirtFrictionFactor, 1.1F, 0.9F),
        SNOW(IWheelType::getSnowFrictionFactor, 1.5F, 0.9F),
        ICE(type -> 1F, 1.5F, 0.01F),
        NONE(type -> 0F, 1.0F, 1.0F);

        private final Function<IWheelType, Float> wheelFunction;
        private final float friction;
        private final float tractionFactor;

        SurfaceType(Function<IWheelType, Float> frictionFunction, float friction, float tractionFactor)
        {
            this.wheelFunction = frictionFunction;
            this.friction = friction;
            this.tractionFactor = tractionFactor;
        }
    }
}