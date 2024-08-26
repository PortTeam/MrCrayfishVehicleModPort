package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageThrowVehicle
{
    public static void encode(MessageThrowVehicle message, FriendlyByteBuf buffer) {}

    public static MessageThrowVehicle decode(FriendlyByteBuf buffer)
    {
        return new MessageThrowVehicle();
    }

    public static void handle(MessageThrowVehicle message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayer player = supplier.get().getSender();
            if(player != null)
            {
                ServerPlayHandler.handleThrowVehicle(player, message);
            }
        });
        supplier.get().setPacketHandled(true);
    }
}
