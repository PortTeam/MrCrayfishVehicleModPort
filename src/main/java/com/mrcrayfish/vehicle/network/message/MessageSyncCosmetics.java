package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ClientPlayHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageSyncCosmetics
{
    private int entityId;
    private List<Pair<ResourceLocation, ResourceLocation>> dirtyEntries;

    public MessageSyncCosmetics() {}

    public MessageSyncCosmetics(int entityId, List<Pair<ResourceLocation, ResourceLocation>> dirtyEntries)
    {
        this.entityId = entityId;
        this.dirtyEntries = dirtyEntries;
    }

    public static void encode(MessageSyncCosmetics message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeInt(message.dirtyEntries.size());
        message.dirtyEntries.forEach(pair -> {
            buffer.writeResourceLocation(pair.getLeft());
            buffer.writeResourceLocation(pair.getRight());
        });
    }

    public static MessageSyncCosmetics decode(FriendlyByteBuf buffer)
    {
        int entityId = buffer.readInt();
        List<Pair<ResourceLocation, ResourceLocation>> dirtyEntries = new ArrayList<>();
        int size = buffer.readInt();
        for(int i = 0; i < size; i++)
        {
            ResourceLocation cosmeticId = buffer.readResourceLocation();
            ResourceLocation modelLocation = buffer.readResourceLocation();
            dirtyEntries.add(Pair.of(cosmeticId, modelLocation));
        }
        return new MessageSyncCosmetics(entityId, dirtyEntries);
    }

    public static void handle(MessageSyncCosmetics message, Supplier<NetworkEvent.Context> supplier)
    {
        if(supplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
        {
            IMessage.enqueueTask(supplier, () -> ClientPlayHandler.handleSyncCosmetics(message));
        }
    }

    public int getEntityId()
    {
        return this.entityId;
    }

    public List<Pair<ResourceLocation, ResourceLocation>> getDirtyEntries()
    {
        return this.dirtyEntries;
    }
}
