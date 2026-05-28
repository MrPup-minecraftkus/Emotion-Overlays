package com.mrpup.emotion_overlays.client;

import com.mrpup.emotion_overlays.EmotionOverlays;
import com.mrpup.emotion_overlays.client.emoji.ClientEventHandler;
import com.mrpup.emotion_overlays.client.emoji.EmojiRenderer;
import com.mrpup.emotion_overlays.client.emoji.EmojiTextureManager;
import com.mrpup.emotion_overlays.client.network.ClientNetworkHandler;
import com.mrpup.emotion_overlays.common.EmojiRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class EmotionOverlaysClient implements ClientModInitializer {

    public static KeyMapping EMOJI_MENU_KEY;

    @Override
    public void onInitializeClient() {

        KeyMapping.Category CATEGORY =
                KeyMapping.Category.register(
                        Identifier.fromNamespaceAndPath(EmotionOverlays.MOD_ID, "emotion_overlays")
                );


        EMOJI_MENU_KEY = new KeyMapping(
                "key.emotion_overlays.emoji_menu",
                GLFW.GLFW_KEY_B,
                CATEGORY
        );
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            EmojiRegistry.load(client.getResourceManager());
            EmojiTextureManager.init();
        });

        ClientEventHandler.register();
        ClientNetworkHandler.registerClient();


        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {

            var collector = context.submitNodeCollector();
            var poseStack = context.poseStack();
            EmojiRenderer.onSubmitCustomGeometry(collector, poseStack);
        });

    }
}