package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ClientPlayHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;


import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageSyncHeldVehicle
{
    private int entityId;
    private CompoundTag vehicleTag;

    public MessageSyncHeldVehicle() {}

    public MessageSyncHeldVehicle(int entityId, CompoundTag vehicleTag)
    {
        this.entityId = entityId;
        this.vehicleTag = vehicleTag;
    }

    public static void encode(MessageSyncHeldVehicle message, FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(message.entityId);
        buffer.writeNbt(message.vehicleTag);
    }

    public static MessageSyncHeldVehicle decode(FriendlyByteBuf buffer)
    {
        return new MessageSyncHeldVehicle(buffer.readVarInt(), buffer.readNbt());
    }

    public static void handle(MessageSyncHeldVehicle message, Supplier<NetworkEvent.Context> supplier)
    {
        if(supplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
        {
            IMessage.enqueueTask(supplier, () -> ClientPlayHandler.handleSyncHeldVehicle(message));
        }
    }

    public int getEntityId()
    {
        return this.entityId;
    }

    public CompoundTag getVehicleTag()
    {
        return this.vehicleTag;
    }
}
