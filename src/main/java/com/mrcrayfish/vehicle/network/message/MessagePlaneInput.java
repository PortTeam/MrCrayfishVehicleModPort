package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessagePlaneInput
{
	private float lift;
	private float forward;
	private float side;

	public MessagePlaneInput() {}

	public MessagePlaneInput(float lift, float forward, float side)
	{
		this.lift = lift;
		this.forward = forward;
		this.side = side;
	}

	public static void encode(MessagePlaneInput message, FriendlyByteBuf buffer)
	{
		buffer.writeFloat(message.lift);
		buffer.writeFloat(message.forward);
		buffer.writeFloat(message.side);
	}

	public static MessagePlaneInput decode(FriendlyByteBuf buffer)
	{
		return new MessagePlaneInput(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
	}

	public static void handle(MessagePlaneInput message, Supplier<NetworkEvent.Context> supplier)
	{
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				ServerPlayHandler.handlePlaneInputMessage(player, message);
			}
		});
		supplier.get().setPacketHandled(true);
	}

	public float getLift()
	{
		return this.lift;
	}

	public float getForward()
	{
		return this.forward;
	}

	public float getSide()
	{
		return this.side;
	}
}
