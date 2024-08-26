package com.mrcrayfish.vehicle.util;


import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

/**
 * Author: MrCrayfish
 */
public enum Axis
{
    X(com.mojang.math.Axis.XP, "x"),
    Y(com.mojang.math.Axis.YP, "y"),
    Z(com.mojang.math.Axis.ZP, "z");

    private final com.mojang.math.Axis axis;
    private final String key;

    Axis(com.mojang.math.Axis axis, String key)
    {
        this.axis = axis;
        this.key = key;
    }

    public com.mojang.math.Axis getAxis()
    {
        return this.axis;
    }

    public String getKey()
    {
        return this.key;
    }

    public static Axis fromKey(String key)
    {
        return Arrays.stream(values()).filter(axis -> axis.key.equals(key)).findFirst().orElse(Axis.X);
    }
}
