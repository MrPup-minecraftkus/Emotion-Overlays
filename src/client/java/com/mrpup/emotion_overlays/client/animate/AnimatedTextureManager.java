package com.mrpup.emotion_overlays.client.animate;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnimatedTextureManager {
    private static final Map<String, AnimatedTexture> ANIMATED = new ConcurrentHashMap<>();
    private static final Map<String, ResourceLocation> LOCATIONS = new ConcurrentHashMap<>();
    private static final Map<String, DynamicTexture> MAIN_TEXTURES = new ConcurrentHashMap<>();

    public static void register(String cpHex, AnimatedTexture animated, ResourceLocation loc) {
        ANIMATED.put(cpHex, animated);
        LOCATIONS.put(cpHex, loc);

        NativeImage mainImage = new NativeImage(
                animated.getWidth(), animated.getHeight(), false);
        DynamicTexture mainTex = new DynamicTexture(mainImage);
        Minecraft.getInstance().getTextureManager().register(loc, mainTex);
        MAIN_TEXTURES.put(cpHex, mainTex);
    }

    public static boolean isAnimated(String cpHex) {
        return ANIMATED.containsKey(cpHex);
    }

    public static ResourceLocation getAndUpdate(String cpHex) {
        AnimatedTexture anim = ANIMATED.get(cpHex);
        ResourceLocation loc = LOCATIONS.get(cpHex);
        if (anim == null || loc == null) return null;

        anim.tick();

        DynamicTexture mainTex = MAIN_TEXTURES.get(cpHex);
        if (mainTex == null) return loc;

        NativeImage currentPixels = anim.getCurrentPixels();
        NativeImage mainImage = mainTex.getPixels();
        if (currentPixels != null && mainImage != null) {
            for (int y = 0; y < currentPixels.getHeight(); y++) {
                for (int x = 0; x < currentPixels.getWidth(); x++) {
                    mainImage.setPixelRGBA(x, y, currentPixels.getPixelRGBA(x, y));
                }
            }
            mainTex.upload();
        }

        return loc;
    }
}
