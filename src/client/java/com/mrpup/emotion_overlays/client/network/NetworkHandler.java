package com.mrpup.emotion_overlays.client.network;


import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class NetworkHandler {

    public static void registerServer() {
        PayloadTypeRegistry.serverboundPlay().register(SelectEmojiPacket.TYPE, SelectEmojiPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BroadcastEmojiPacket.TYPE, BroadcastEmojiPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SelectEmojiPacket.TYPE, SelectEmojiPacket::handle);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(BroadcastEmojiPacket.TYPE, BroadcastEmojiPacket::handle);
    }
}

