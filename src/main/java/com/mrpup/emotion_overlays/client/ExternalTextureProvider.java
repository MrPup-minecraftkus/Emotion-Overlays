package com.mrpup.emotion_overlays.client;

public interface ExternalTextureProvider {
    boolean isPending(String cpHex);
    void requestLoad(String cpHex);
}
