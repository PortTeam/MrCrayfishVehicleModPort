package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


/**
 * Author: MrCrayfish
 */
public class MessageCycleSeats
{
    public MessageCycleSeats() {}

    public static void encode(MessageCycleSeats message, FriendlyByteBuf buffer) {}

    public static MessageCycleSeats decode(FriendlyByteBuf buffer)
    {
        return new MessageCycleSeats();
    }

    public static void handle(MessageCycleSeats message, Supplier<NetworkEvent.Context> supplier)
    {
        if(supplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER)
        {
            supplier.get().enqueueWork(() ->
            {
                ServerPlayer player = supplier.get().getSender();
                if(player != null)
                {
                    ServerPlayHandler.handleCycleSeatsMessage(player, message);
                }
            });
            supplier.get().setPacketHandled(true);
        }
    }
}
