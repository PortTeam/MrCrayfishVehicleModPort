package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageOpenStorage
{
    private int entityId;
    private String key;

    public MessageOpenStorage() {}

    public MessageOpenStorage(int entityId, String key)
    {
        this.entityId = entityId;
        this.key = key;
    }

    public static void encode(MessageOpenStorage message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeUtf(message.key);
    }

    public static MessageOpenStorage decode(FriendlyByteBuf buffer)
    {
        return new MessageOpenStorage(buffer.readInt(), buffer.readUtf());
    }

    public static void handle(MessageOpenStorage message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayer player = supplier.get().getSender();
            if(player != null)
            {
                ServerPlayHandler.handleOpenStorageMessage(player, message);
            }
        });
        supplier.get().setPacketHandled(true);
    }

    public int getEntityId()
    {
        return this.entityId;
    }

    public String getKey()
    {
        return this.key;
    }
}
