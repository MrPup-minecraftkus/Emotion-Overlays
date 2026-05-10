package com.mrpup.emotion_overlays.client;

import com.mrpup.emotion_overlays.EmotionOverlays;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = EmotionOverlays.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) return;
        if (mc.screen != null) return;

        while (ClientSetup.EMOJI_MENU_KEY.consumeClick()) {
            mc.setScreen(new EmojiScreen());
        }
    }
}
