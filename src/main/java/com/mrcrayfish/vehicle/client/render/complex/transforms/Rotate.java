package com.mrcrayfish.vehicle.client.render.complex.transforms;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.client.render.complex.value.Dynamic;
import com.mrcrayfish.vehicle.client.render.complex.value.IValue;
import com.mrcrayfish.vehicle.client.render.complex.value.Static;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.lang.reflect.Type;

/**
 * Author: MrCrayfish
 */
public class Rotate implements Transform
{
    private final IValue x, y, z;

    public Rotate(IValue x, IValue y, IValue z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void apply(VehicleEntity entity, PoseStack stack, float partialTicks)
    {
        stack.mulPose(Axis.XP.rotationDegrees((float) this.x.getValue(entity, partialTicks)));
        stack.mulPose(Axis.YP.rotationDegrees((float) this.y.getValue(entity, partialTicks)));
        stack.mulPose(Axis.ZP.rotationDegrees((float) this.z.getValue(entity, partialTicks)));
    }

    @Override
    public Matrix4f create(VehicleEntity entity, float partialTicks)
    {
        Matrix4f matrix4f = new Matrix4f();
        float xRot = (float) this.x.getValue(entity, partialTicks);
        float yRot = (float) this.y.getValue(entity, partialTicks);
        float zRot = (float) this.z.getValue(entity, partialTicks);

        Quaternionf quaternion = new Quaternionf().rotationXYZ(xRot, yRot, zRot);
        return matrix4f.rotate(quaternion);
    }


    public static class Deserializer implements JsonDeserializer<Rotate>
    {
        @Override
        public Rotate deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject object = json.getAsJsonObject();
            IValue x = this.get(object, "x", context);
            IValue y = this.get(object, "y", context);
            IValue z = this.get(object, "z", context);
            return new Rotate(x, y, z);
        }

        private IValue get(JsonObject object, String key, JsonDeserializationContext context)
        {
            if(!object.has(key)) return Static.ZERO;
            JsonElement e = object.get(key);
            if(e.isJsonObject())
            {
                return context.deserialize(e, Dynamic.class);
            }
            else if(e.isJsonPrimitive())
            {
                return context.deserialize(e, Static.class);
            }
            throw new JsonParseException("Rotate values can only be a number or object");
        }
    }
}
