package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


/**
 * Author: MrCrayfish
 */
public class MessageCraftVehicle
{
    private String vehicleId;
    private BlockPos pos;

    public MessageCraftVehicle() {}

    public MessageCraftVehicle(String vehicleId, BlockPos pos)
    {
        this.vehicleId = vehicleId;
        this.pos = pos;
    }

    public static void encode(MessageCraftVehicle message, FriendlyByteBuf buffer)
    {
        buffer.writeUtf(message.vehicleId, 128);
        buffer.writeBlockPos(message.pos);
    }

    public static MessageCraftVehicle decode(FriendlyByteBuf buffer)
    {
        return new MessageCraftVehicle(buffer.readUtf(128), buffer.readBlockPos());
    }

    public static void handle(MessageCraftVehicle message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayer player = supplier.get().getSender();
            if(player != null)
            {
                ServerPlayHandler.handleCraftVehicleMessage(player, message);
            }
        });
        supplier.get().setPacketHandled(true);
    }

    public String getVehicleId()
    {
        return this.vehicleId;
    }

    public BlockPos getPos()
    {
        return this.pos; //TODO should be able to derive from the container instead
    }
}