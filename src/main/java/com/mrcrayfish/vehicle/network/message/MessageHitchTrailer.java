package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;


import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageHitchTrailer
{
    private boolean hitch;

    public MessageHitchTrailer() {}

    public MessageHitchTrailer(boolean hitch)
    {
        this.hitch = hitch;
    }

    public static void encode(MessageHitchTrailer message, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(message.hitch);
    }

    public static MessageHitchTrailer decode(FriendlyByteBuf buffer)
    {
        return new MessageHitchTrailer(buffer.readBoolean());
    }

    public static void handle(MessageHitchTrailer message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayer player = supplier.get().getSender();
            if(player != null)
            {
                ServerPlayHandler.handleHitchTrailerMessage(player, message);
            }
        });
        supplier.get().setPacketHandled(true);
    }

    public boolean isHitch()
    {
        return this.hitch;
    }
}
