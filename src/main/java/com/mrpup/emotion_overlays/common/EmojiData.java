package com.mrpup.emotion_overlays.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EmojiData {

    private static final Map<UUID, ActiveEmoji> ACTIVE = new HashMap<>();

    public static void setEmoji(UUID playerId, EmojiEntry emoji) {
        ACTIVE.put(playerId, new ActiveEmoji(emoji, System.currentTimeMillis()));
    }

    public static void clearEmoji(UUID playerId) {
        ACTIVE.remove(playerId);
    }

    public static ActiveEmoji getEmoji(UUID playerId) {
        ActiveEmoji a = ACTIVE.get(playerId);
        if (a == null) return null;
        if (System.currentTimeMillis() - a.timestamp() > 5000) {
            ACTIVE.remove(playerId);
            return null;
        }
        return a;
    }

    public static boolean hasEmoji(UUID playerId) {
        return getEmoji(playerId) != null;
    }

    public record ActiveEmoji(EmojiEntry emoji, long timestamp) {
        public float getAlpha() {
            long elapsed = System.currentTimeMillis() - timestamp;
            if (elapsed < 4000) return 1.0f;
            if (elapsed >= 5000) return 0.0f;
            return 1.0f - (float)(elapsed - 4000) / 1000f;
        }

        public float getScale() {
            long elapsed = System.currentTimeMillis() - timestamp;
            if (elapsed < 200) return 0.5f + (float)(elapsed / 200.0) * 0.5f;
            return 1.0f;
        }
    }
}
