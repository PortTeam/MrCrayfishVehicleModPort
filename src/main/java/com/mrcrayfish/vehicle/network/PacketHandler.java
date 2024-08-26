package com.mrcrayfish.vehicle.network;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.network.message.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketHandler
{
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel HANDSHAKE_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(Reference.MOD_ID, "handshake"), () -> PROTOCOL_VERSION, s -> true, s -> true);
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Reference.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int nextId = 0;

    public static void registerMessages()
    {
        HANDSHAKE_CHANNEL.messageBuilder(HandshakeMessages.C2SAcknowledge.class, 99)
                .loginIndex(HandshakeMessages.LoginIndexedMessage::getLoginIndex, HandshakeMessages.LoginIndexedMessage::setLoginIndex)
                .decoder(HandshakeMessages.C2SAcknowledge::decode)
                .encoder(HandshakeMessages.C2SAcknowledge::encode)
                .consumerMainThread(net.minecraftforge.network.HandshakeHandler.indexFirst((handler, msg, s) -> HandshakeHandler.handleAcknowledge(msg, s)))
                .add();

        HANDSHAKE_CHANNEL.messageBuilder(HandshakeMessages.S2CVehicleProperties.class, 1)
                .loginIndex(HandshakeMessages.LoginIndexedMessage::getLoginIndex, HandshakeMessages.LoginIndexedMessage::setLoginIndex)
                .decoder(HandshakeMessages.S2CVehicleProperties::decode)
                .encoder(HandshakeMessages.S2CVehicleProperties::encode)
                .consumerMainThread(net.minecraftforge.network.HandshakeHandler.biConsumerFor((handler, msg, supplier) -> HandshakeHandler.handleVehicleProperties(msg, supplier)))
                .markAsLoginPacket()
                .add();
        HANDSHAKE_CHANNEL.messageBuilder(HandshakeMessages.S2CSyncedPlayerData.class, 1)
                .loginIndex(HandshakeMessages.LoginIndexedMessage::getLoginIndex, HandshakeMessages.LoginIndexedMessage::setLoginIndex)
                .decoder(HandshakeMessages.S2CSyncedPlayerData::decode)
                .encoder(HandshakeMessages.S2CSyncedPlayerData::encode)
                .consumerMainThread(net.minecraftforge.network.HandshakeHandler.biConsumerFor((handler, msg, supplier) -> HandshakeHandler.handleSyncedPlayerData(msg, supplier)))
                .markAsLoginPacket()
                .add();
        registerMessage(MessageTurnAngle.class, MessageTurnAngle::encode, MessageTurnAngle::decode, MessageTurnAngle::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageHandbrake.class, MessageHandbrake::encode, MessageHandbrake::decode, MessageHandbrake::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageHorn.class, MessageHorn::encode, MessageHorn::decode, MessageHorn::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageThrowVehicle.class, MessageThrowVehicle::encode, MessageThrowVehicle::decode, MessageThrowVehicle::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessagePickupVehicle.class, MessagePickupVehicle::encode, MessagePickupVehicle::decode, MessagePickupVehicle::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageAttachChest.class, MessageAttachChest::encode, MessageAttachChest::decode, MessageAttachChest::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageAttachTrailer.class, MessageAttachTrailer::encode, MessageAttachTrailer::decode, MessageAttachTrailer::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageFuelVehicle.class, MessageFuelVehicle::encode, MessageFuelVehicle::decode, MessageFuelVehicle::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageInteractKey.class, MessageInteractKey::encode, MessageInteractKey::decode, MessageInteractKey::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageHelicopterInput.class, MessageHelicopterInput::encode, MessageHelicopterInput::decode, MessageHelicopterInput::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageCraftVehicle.class, MessageCraftVehicle::encode, MessageCraftVehicle::decode, MessageCraftVehicle::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageHitchTrailer.class, MessageHitchTrailer::encode, MessageHitchTrailer::decode, MessageHitchTrailer::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageSyncStorage.class, MessageSyncStorage::encode, MessageSyncStorage::decode, MessageSyncStorage::handle, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(MessageOpenStorage.class, MessageOpenStorage::encode, MessageOpenStorage::decode, MessageOpenStorage::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageThrottle.class, MessageThrottle::encode, MessageThrottle::decode, MessageThrottle::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageEntityFluid.class, MessageEntityFluid::encode, MessageEntityFluid::decode, MessageEntityFluid::handle, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(MessageSyncPlayerSeat.class, MessageSyncPlayerSeat::encode, MessageSyncPlayerSeat::decode, MessageSyncPlayerSeat::handle, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(MessageCycleSeats.class, MessageCycleSeats::encode, MessageCycleSeats::decode, MessageCycleSeats::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageSetSeat.class, MessageSetSeat::encode, MessageSetSeat::decode, MessageSetSeat::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageSyncHeldVehicle.class, MessageSyncHeldVehicle::encode, MessageSyncHeldVehicle::decode, MessageSyncHeldVehicle::handle, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(MessagePlaneInput.class, MessagePlaneInput::encode, MessagePlaneInput::decode, MessagePlaneInput::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageSyncCosmetics.class, MessageSyncCosmetics::encode, MessageSyncCosmetics::decode, MessageSyncCosmetics::handle, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(MessageInteractCosmetic.class, MessageInteractCosmetic::encode, MessageInteractCosmetic::decode, MessageInteractCosmetic::handle, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(MessageSyncActionData.class, MessageSyncActionData::encode, MessageSyncActionData::decode, MessageSyncActionData::handle, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(MessageSyncPlayerData.class, MessageSyncPlayerData::encode, MessageSyncPlayerData::decode, MessageSyncPlayerData::handle, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static <T> void registerMessage(Class<T> clazz, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> handler, NetworkDirection direction)
    {
        CHANNEL.messageBuilder(clazz, nextId++, direction)
                .encoder(encoder)
                .decoder(decoder)
                .consumerMainThread(handler)
                .add();
    }

    public static SimpleChannel getChannel()
    {
        return CHANNEL;
    }
    public static SimpleChannel getHandshakeChannel()
    {
        return HANDSHAKE_CHANNEL;
    }

    public static <T> void sendToServer(T message)
    {
        CHANNEL.sendToServer(message);
    }

    public static <T> void sendToClient(T message, ServerPlayer player)
    {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <T> void sendToAllClients(T message)
    {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }
}
