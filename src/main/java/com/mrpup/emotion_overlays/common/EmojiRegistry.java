package com.mrpup.emotion_overlays.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mrpup.emotion_overlays.EmotionOverlays;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class EmojiRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmotionOverlays.MOD_ID);
    public static final List<EmojiEntry> ALL = new ArrayList<>();
    public static final LinkedHashMap<String, List<EmojiEntry>> BY_CATEGORY = new LinkedHashMap<>();
    private static final Map<String, EmojiEntry> BY_CP = new HashMap<>();
    private static boolean loaded = false;

    public static void load(ResourceManager rm) {
        if (loaded) return;
        loaded = true;

        Identifier loc = Identifier.fromNamespaceAndPath(
                EmotionOverlays.MOD_ID, "emoji.json");

        try (InputStream is = rm.getResourceOrThrow(loc).open()) {
            JsonArray root = JsonParser.parseReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonArray();

            for (JsonElement groupEl : root) {
                JsonObject groupObj = groupEl.getAsJsonObject();
                String categoryName = groupObj.get("name").getAsString();
                List<EmojiEntry> catList = new ArrayList<>();

                for (JsonElement eEl : groupObj.getAsJsonArray("emojis")) {
                    JsonObject e = eEl.getAsJsonObject();

                    String character = e.get("emoji").getAsString();
                    String name = e.get("name").getAsString();

                    String cpHex = convertToHex(character);

                    EmojiEntry entry = new EmojiEntry(
                            character,
                            cpHex,
                            name,
                            categoryName
                    );

                    ALL.add(entry);
                    catList.add(entry);
                    BY_CP.put(cpHex, entry);
                }
                BY_CATEGORY.put(categoryName, catList);
            }

            LOGGER.info("[EmotionOverlays] download {} emojis", ALL.size());

        } catch (Exception e) {
            LOGGER.error("[EmotionOverlays] cannot download emoji.json", e);
        }
    }

    private static String convertToHex(String emoji) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < emoji.length(); ) {
            int codePoint = emoji.codePointAt(i);
            if (sb.length() > 0) sb.append("_");
            sb.append(Integer.toHexString(codePoint).toLowerCase());
            i += Character.charCount(codePoint);
        }
        return sb.toString()
                .replace("_fe0f", "");
    }

    public static int indexOf(EmojiEntry entry) { return ALL.indexOf(entry); }

    public static EmojiEntry byIndex(int index) {
        if (index < 0 || index >= ALL.size()) return ALL.isEmpty() ? null : ALL.get(0);
        return ALL.get(index);
    }

    public static EmojiEntry byCp(String cpHex) { return BY_CP.get(cpHex); }

    public static List<EmojiEntry> search(String query) {
        if (query == null || query.isBlank()) return Collections.emptyList();

        if (externalSearchHook != null && query.length() >= 2) {
            externalSearchHook.accept(query);
        }

        String q = query.toLowerCase(Locale.ROOT).trim();
        List<EmojiEntry> results = new ArrayList<>();
        for (EmojiEntry e : ALL) {
            if (e.name().toLowerCase(Locale.ROOT).contains(q)) {
                results.add(e);
                if (results.size() >= 100) break;
            }
        }
        return results;
    }

    public static List<String> categoryNames() {
        return new ArrayList<>(BY_CATEGORY.keySet());
    }

    public static void registerEntry(EmojiEntry entry) {
        ALL.add(entry);
        BY_CATEGORY.computeIfAbsent(entry.category(), k -> new ArrayList<>()).add(entry);
    }

    private static java.util.function.Consumer<String> externalSearchHook = null;

    public static void setExternalSearchHook(java.util.function.Consumer<String> hook) {
        externalSearchHook = hook;
    }
}