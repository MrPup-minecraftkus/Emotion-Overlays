package com.mrpup.emotion_overlays.client.network;

import com.mrpup.emotion_overlays.common.EmojiData;
import com.mrpup.emotion_overlays.common.EmojiEntry;
import com.mrpup.emotion_overlays.common.EmojiRegistry;
import com.mrpup.emotion_overlays.network.BroadcastEmojiPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientNetworkHandler {
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(BroadcastEmojiPacket.TYPE, (packet, context) -> {
            EmojiEntry emoji = EmojiRegistry.byCp(packet.emojiCpHex());
            if (emoji == null) {
                emoji = new EmojiEntry(packet.emojiCpHex(), packet.emojiCpHex(), packet.emojiCpHex(), "7TV");
            }
            EmojiData.setEmoji(packet.playerId(), emoji);
        });
    }
}
