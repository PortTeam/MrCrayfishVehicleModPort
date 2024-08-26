package com.mrcrayfish.vehicle.client.raytrace;

import com.mrcrayfish.vehicle.client.raytrace.data.RayTraceData;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public interface RayTraceTransforms
{
    void load(EntityRayTracer tracer, List<Matrix4f> transforms, HashMap<RayTraceData, List<Matrix4f>> parts);
}
