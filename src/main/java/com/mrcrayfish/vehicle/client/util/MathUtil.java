package com.mrcrayfish.vehicle.client.util;

import net.minecraft.util.Mth;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class MathUtil
{
    /**
     * Spherically interpolates between two quaternions with a weight
     * Source taken from Bones framework for JPCT, see <a href="https://github.com/raftAtGit/Bones">...</a>
     * Code has been adapted to work with Quaternion from Minecraft's math package.
     *
     * @param start the starting quaternion
     * @param end the destination quaternion
     * @param t the weight of the interpolation in the range of [0, 1]
     */
    public static Quaternionf slerp(Quaternionf start, Quaternionf end, float t)
    {
        // If the quaternions are the same, no interpolation is needed.
        if (start.equals(end))
        {
            return new Quaternionf(start);
        }

        // Compute the dot product of the two quaternions
        float dot = start.dot(end);

        // If the dot product is negative, the quaternions have opposite handedness
        // and slerp won't take the shorter path. Invert one quaternion.
        if (dot < 0.0f)
        {
            end = new Quaternionf(-end.x(), -end.y(), -end.z(), -end.w());
            dot = -dot;
        }

        // Clamp dot product to avoid potential numerical errors
        dot = Mth.clamp(dot, -1.0f, 1.0f);

        float scale0 = 1 - t;
        float scale1 = t;

        // If the dot product is very close to 1, the quaternions are nearly parallel
        // and linear interpolation is safe
        if (1.0f - dot > 0.1f)
        {
            // Calculate the angle between the quaternions
            float theta = (float) Math.acos(dot);
            float sinTheta = Mth.sin(theta);

            // Compute the interpolation scales
            scale0 = Mth.sin((1.0f - t) * theta) / sinTheta;
            scale1 = Mth.sin(t * theta) / sinTheta;
        }

        // Calculate the interpolated quaternion
        float x = (scale0 * start.x()) + (scale1 * end.x());
        float y = (scale0 * start.y()) + (scale1 * end.y());
        float z = (scale0 * start.z()) + (scale1 * end.z());
        float w = (scale0 * start.w()) + (scale1 * end.w());

        return new Quaternionf(x, y, z, w);
    }

}
