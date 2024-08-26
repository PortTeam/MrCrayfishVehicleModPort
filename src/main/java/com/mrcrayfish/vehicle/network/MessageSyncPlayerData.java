package com.mrcrayfish.vehicle.network;

import com.mrcrayfish.obfuscate.common.data.SyncedPlayerData;
import com.mrcrayfish.vehicle.client.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MessageSyncPlayerData
{
    private int entityId;
    private List<SyncedPlayerData.DataEntry<?>> entries;

    public MessageSyncPlayerData() {}

    public MessageSyncPlayerData(int entityId, List<SyncedPlayerData.DataEntry<?>> entries)
    {
        this.entityId = entityId;
        this.entries = entries;
    }

    public static void encode(MessageSyncPlayerData message, FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(message.entityId);
        buffer.writeVarInt(message.entries.size());
        message.entries.forEach(entry -> entry.write(buffer));
    }

    public static MessageSyncPlayerData decode(FriendlyByteBuf buffer)
    {
        int entityId = buffer.readVarInt();
        int size = buffer.readVarInt();
        List<SyncedPlayerData.DataEntry<?>> entries = new ArrayList<>();
        for(int i = 0; i < size; i++)
        {
            entries.add(SyncedPlayerData.DataEntry.read(buffer));
        }
        return new MessageSyncPlayerData(entityId, entries);
    }

    public static void handle(MessageSyncPlayerData message, Supplier<NetworkEvent.Context> supplier)
    {
        if(supplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
        {
            supplier.get().enqueueWork(() -> ClientHandler.instance().updatePlayerData(message.entityId, message.entries));
            supplier.get().setPacketHandled(true);
        }
    }
}