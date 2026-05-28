package com.mrpup.emotion_overlays;

import com.mrpup.emotion_overlays.network.NetworkHandler;
import net.fabricmc.api.ModInitializer;

public class EmotionOverlays implements ModInitializer {
	public static final String MOD_ID = "emotion_overlays";

	@Override
	public void onInitialize() {
        NetworkHandler.registerServer();
	}
}