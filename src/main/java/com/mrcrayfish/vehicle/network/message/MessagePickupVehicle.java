package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;


import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessagePickupVehicle
{
    private int entityId;

    public MessagePickupVehicle()
    {
    }

    public MessagePickupVehicle(Entity targetEntity)
    {
        this.entityId = targetEntity.getId();
    }

    public MessagePickupVehicle(int entityId)
    {
        this.entityId = entityId;
    }

    public static void encode(MessagePickupVehicle message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
    }

    public static MessagePickupVehicle decode(FriendlyByteBuf buffer)
    {
        return new MessagePickupVehicle(buffer.readInt());
    }


    public static void handle(MessagePickupVehicle message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayer player = supplier.get().getSender();
            if(player != null)
            {
                ServerPlayHandler.handlePickupVehicleMessage(player, message);
            }
        });
        supplier.get().setPacketHandled(true);
    }

    public int getEntityId()
    {
        return this.entityId;
    }
}