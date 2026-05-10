package com.mrpup.emotion_overlays.network;

import com.mrpup.emotion_overlays.EmotionOverlays;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = EmotionOverlays.MOD_ID)
public class NetworkHandler {

    public static void register() {

    }

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                SelectEmojiPacket.TYPE,
                SelectEmojiPacket.STREAM_CODEC,
                SelectEmojiPacket::handle
        );

        registrar.playToClient(
                BroadcastEmojiPacket.TYPE,
                BroadcastEmojiPacket.STREAM_CODEC,
                BroadcastEmojiPacket::handle
        );
    }
}
