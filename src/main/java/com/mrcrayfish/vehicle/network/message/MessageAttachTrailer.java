package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


/**
 * Author: MrCrayfish
 */
public class MessageAttachTrailer
{
    private int trailerId;

    public MessageAttachTrailer() {}

    public MessageAttachTrailer(int trailerId)
    {
        this.trailerId = trailerId;
    }

    public static void encode(MessageAttachTrailer message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.trailerId);
    }

    public static MessageAttachTrailer decode(FriendlyByteBuf buffer)
    {
        return new MessageAttachTrailer(buffer.readInt());
    }

    public static void handle(MessageAttachTrailer message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayer player = supplier.get().getSender();
            if(player != null)
            {
                ServerPlayHandler.handleAttachTrailerMessage(player, message);
            }
        });
        supplier.get().setPacketHandled(true);
    }

    public int getTrailerId()
    {
        return this.trailerId;
    }
}
