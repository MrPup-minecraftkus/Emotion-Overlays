package com.mrpup.emotion_overlays.client.emoji;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.mrpup.emotion_overlays.client.EmotionOverlaysClient;

public class ClientEventHandler {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) return;
            if (client.screen != null) return;

            while (EmotionOverlaysClient.EMOJI_MENU_KEY.consumeClick()) {
                client.setScreen(new EmojiScreen());
            }
        });
    }
}
