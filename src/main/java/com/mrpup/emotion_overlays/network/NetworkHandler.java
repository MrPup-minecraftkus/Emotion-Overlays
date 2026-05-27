package com.mrpup.emotion_overlays.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class NetworkHandler {

    public static void registerServer() {
        PayloadTypeRegistry.playC2S().register(SelectEmojiPacket.TYPE, SelectEmojiPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(BroadcastEmojiPacket.TYPE, BroadcastEmojiPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SelectEmojiPacket.TYPE, SelectEmojiPacket::handle);
    }
}

