package com.mrpup.emotion_overlays.common;

public record EmojiEntry(
        String character,
        String cpHex,
        String name,
        String category
) {
    public int index() {
        return EmojiRegistry.indexOf(this);
    }
}
