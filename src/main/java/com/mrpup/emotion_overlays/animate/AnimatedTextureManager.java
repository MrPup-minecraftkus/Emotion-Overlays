package com.mrpup.emotion_overlays.animate;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnimatedTextureManager {
    private static final Map<String, AnimatedTexture> ANIMATED = new ConcurrentHashMap<>();
    private static final Map<String, Identifier> LOCATIONS = new ConcurrentHashMap<>();
    private static final Map<String, DynamicTexture> MAIN_TEXTURES = new ConcurrentHashMap<>();



    public static void register(String cpHex, AnimatedTexture animated, Identifier loc) {
        ANIMATED.put(cpHex, animated);
        LOCATIONS.put(cpHex, loc);

        DynamicTexture mainTex = new DynamicTexture(
                "emoji_" + cpHex,
                animated.getWidth(),
                animated.getHeight(),
                false
        );

        Minecraft.getInstance().getTextureManager().register(loc, mainTex);
        MAIN_TEXTURES.put(cpHex, mainTex);
    }

    public static boolean isAnimated(String cpHex) {
        return ANIMATED.containsKey(cpHex);
    }

    public static Identifier getAndUpdate(String cpHex) {
        AnimatedTexture anim = ANIMATED.get(cpHex);
        Identifier loc = LOCATIONS.get(cpHex);
        if (anim == null || loc == null) return null;

        anim.tick();

        DynamicTexture mainTex = MAIN_TEXTURES.get(cpHex);
        if (mainTex == null) return loc;

        NativeImage currentPixels = anim.getCurrentPixels();
        if (currentPixels == null) return loc;


        NativeImage copy = new NativeImage(
                currentPixels.getWidth(),
                currentPixels.getHeight(),
                false
        );
        copyPixels(currentPixels, copy);

        mainTex.setPixels(copy);
        mainTex.upload();

        return loc;
    }

    private static void copyPixels(NativeImage src, NativeImage dst) {
        dst.copyFrom(src);
    }
}
