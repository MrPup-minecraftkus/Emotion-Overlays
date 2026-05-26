package com.mrpup.emotion_overlays.client.animate;



import com.mojang.blaze3d.platform.NativeImage;

import java.util.List;

public class AnimatedTexture {

    public record Frame(NativeImage pixels, int delayMs) {}

    private final List<Frame> frames;
    private int currentFrame = 0;
    private long lastSwitch = 0;

    public AnimatedTexture(List<Frame> frames) {
        this.frames = frames;
    }

    public void tick() {
        long now = System.currentTimeMillis();
        Frame f = frames.get(currentFrame);
        if (now - lastSwitch >= f.delayMs()) {
            currentFrame = (currentFrame + 1) % frames.size();
            lastSwitch = now;
        }
    }

    public NativeImage getCurrentPixels() {
        return frames.get(currentFrame).pixels();
    }

    public int frameCount() { return frames.size(); }

    public int getWidth() { return frames.get(0).pixels().getWidth(); }
    public int getHeight() { return frames.get(0).pixels().getHeight(); }
}
