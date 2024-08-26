package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;


import java.util.function.Supplier;

public class MessageTurnAngle
{
	private float angle;

	public MessageTurnAngle() {}

	public MessageTurnAngle(float angle)
	{
		this.angle = angle;
	}

	public static void encode(MessageTurnAngle message, FriendlyByteBuf buffer)
	{
		buffer.writeFloat(message.angle);
	}

	public static MessageTurnAngle decode(FriendlyByteBuf buffer)
	{
		return new MessageTurnAngle(buffer.readFloat());
	}

	public static void handle(MessageTurnAngle message, Supplier<NetworkEvent.Context> supplier)
	{
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				ServerPlayHandler.handleTurnAngleMessage(player, message);
			}
		});
		supplier.get().setPacketHandled(true);
	}

	public float getAngle()
	{
		return this.angle;
	}
}
