package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageThrottle
{
	private float power;

	public MessageThrottle() {}

	public MessageThrottle(float power)
	{
		this.power = power;
	}

	public static void encode(MessageThrottle message, FriendlyByteBuf buffer)
	{
		buffer.writeFloat(message.power);
	}

	public static MessageThrottle decode(FriendlyByteBuf buffer)
	{
		return new MessageThrottle(buffer.readFloat());
	}

	public static void handle(MessageThrottle message, Supplier<NetworkEvent.Context> supplier)
	{
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				ServerPlayHandler.handleThrottleMessage(player, message);
			}
		});
		supplier.get().setPacketHandled(true);
	}

	public float getPower()
	{
		return this.power;
	}
}