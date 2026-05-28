package com.mrpup.emotion_overlays.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class NetworkHandler {

    public static void registerServer() {
        PayloadTypeRegistry.serverboundPlay().register(SelectEmojiPacket.TYPE, SelectEmojiPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BroadcastEmojiPacket.TYPE, BroadcastEmojiPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SelectEmojiPacket.TYPE, SelectEmojiPacket::handle);
    }
}

