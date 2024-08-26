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
public class MessageInteractKey
{
    private int entityId;

    public MessageInteractKey()
    {
    }

    public MessageInteractKey(Entity targetEntity)
    {
        this.entityId = targetEntity.getId();
    }

    private MessageInteractKey(int entityId)
    {
        this.entityId = entityId;
    }

    public static void encode(MessageInteractKey message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
    }

    public static MessageInteractKey decode(FriendlyByteBuf buffer)
    {
        return new MessageInteractKey(buffer.readInt());
    }

    @SuppressWarnings("ConstantConditions")
    public static void handle(MessageInteractKey message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayer player = supplier.get().getSender();
            if(player != null)
            {
                ServerPlayHandler.handleInteractKeyMessage(player, message);
            }
        });
        supplier.get().setPacketHandled(true);
    }

    public int getEntityId()
    {
        return this.entityId;
    }
}
