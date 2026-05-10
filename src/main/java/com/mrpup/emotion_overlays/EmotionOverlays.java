package com.mrpup.emotion_overlays;

import com.mrpup.emotion_overlays.network.NetworkHandler;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;


@Mod(EmotionOverlays.MOD_ID)
public class EmotionOverlays {
    public static final String MOD_ID = "emotion_overlays";

    public EmotionOverlays(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.register();
    }

    public static void register(String id, ResourceLocation texture) {
    }
}
