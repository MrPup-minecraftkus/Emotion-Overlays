package com.mrpup.emotion_overlays.client.emoji;

public interface ExternalTextureProvider {
    boolean isPending(String cpHex);
    void requestLoad(String cpHex);
}
