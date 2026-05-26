package com.mrpup.emotion_overlays.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrpup.emotion_overlays.client.emoji.ClientEventHandler;
import com.mrpup.emotion_overlays.client.emoji.EmojiRenderer;
import com.mrpup.emotion_overlays.client.emoji.EmojiTextureManager;
import com.mrpup.emotion_overlays.client.network.NetworkHandler;
import com.mrpup.emotion_overlays.common.EmojiRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class EmotionOverlaysClient implements ClientModInitializer {

    public static KeyMapping EMOJI_MENU_KEY;

	@Override
	public void onInitializeClient() {

        EMOJI_MENU_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.emotion_overlays.emoji_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.categories.emotion_overlays"
        ));

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            EmojiRegistry.load(client.getResourceManager());
            EmojiTextureManager.init();
        });

        ClientEventHandler.register();
        EmojiRenderer.register();
        NetworkHandler.registerServer();
        NetworkHandler.registerClient();
	}
}