package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageHorn
{
	private boolean horn;

	public MessageHorn() {}

	public MessageHorn(boolean horn)
	{
		this.horn = horn;
	}

	public static void encode(MessageHorn message, FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(message.horn);
	}


	public static MessageHorn decode(FriendlyByteBuf buffer)
	{
		return new MessageHorn(buffer.readBoolean());
	}


	public static void handle(MessageHorn message, Supplier<NetworkEvent.Context> supplier)
	{
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				ServerPlayHandler.handleHornMessage(player, message);
			}
		});
		supplier.get().setPacketHandled(true);
	}

	public boolean isHorn()
	{
		return this.horn;
	}
}
