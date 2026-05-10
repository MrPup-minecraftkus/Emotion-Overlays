package com.mrpup.emotion_overlays.client;

import com.mrpup.emotion_overlays.EmotionOverlays;
import com.mrpup.emotion_overlays.common.EmojiRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = EmotionOverlays.MOD_ID, value = Dist.CLIENT)
public class ClientSetup {

    public static KeyMapping EMOJI_MENU_KEY;

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        EMOJI_MENU_KEY = new KeyMapping(
                "key.emotion_overlays.emoji_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.categories.emotion_overlays"
        );
        event.register(EMOJI_MENU_KEY);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EmojiRegistry.load(Minecraft.getInstance().getResourceManager());
            EmojiTextureManager.init();
        });
    }
}
