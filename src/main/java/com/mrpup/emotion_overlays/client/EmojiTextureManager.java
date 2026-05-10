package com.mrpup.emotion_overlays.client;

import com.mrpup.emotion_overlays.EmotionOverlays;
import com.mrpup.emotion_overlays.animate.AnimatedTextureManager;
import com.mrpup.emotion_overlays.common.EmojiEntry;
import com.mrpup.emotion_overlays.common.EmojiRegistry;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class EmojiTextureManager {

    private static final Logger LOG = LoggerFactory.getLogger(EmotionOverlays.MOD_ID);

    private static final String CDN = "https://fonts.gstatic.com/s/e/notoemoji/latest/%s/72.png";

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final ExecutorService POOL = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "noto-emoji-loader");
        t.setDaemon(true);
        return t;
    });

    private static final Map<String, ResourceLocation> LOADED = new ConcurrentHashMap<>();
    private static final Set<String> IN_FLIGHT = ConcurrentHashMap.newKeySet();

    private static Path cacheDir;

    public static void init() {
        cacheDir = Minecraft.getInstance().gameDirectory.toPath()
                .resolve("emotion_overlays_cache");
        try { Files.createDirectories(cacheDir); }
        catch (IOException e) { LOG.error("Cannot create emoji cache dir", e); }
        LOG.info("[EmotionOverlays] EmojiTextureManager ready, cache at {}", cacheDir);
    }

    public static ResourceLocation getTexture(EmojiEntry entry) {
        if (AnimatedTextureManager.isAnimated(entry.cpHex())) {
            return AnimatedTextureManager.getAndUpdate(entry.cpHex());
        }

        ResourceLocation loc = LOADED.get(entry.cpHex());
        if (loc != null) return loc;
        schedule(entry);
        return null;
    }

    public static void prefetch(List<EmojiEntry> entries) {
        for (EmojiEntry e : entries) schedule(e);
    }

    public static boolean isLoaded(EmojiEntry entry) {
        return LOADED.containsKey(entry.cpHex());
    }

    public static boolean isLoaded(String cpHex) {
        return LOADED.containsKey(cpHex);
    }

    public static int totalLoaded() { return LOADED.size(); }
    public static int totalEmojis() { return EmojiRegistry.ALL.size(); }


    private static ExternalTextureProvider externalProvider = null;

    public static void setExternalProvider(ExternalTextureProvider provider) {
        externalProvider = provider;
    }

    private static void schedule(EmojiEntry entry) {
        if (LOADED.containsKey(entry.cpHex())) return;
        if (AnimatedTextureManager.isAnimated(entry.cpHex())) return;

        if (externalProvider != null && externalProvider.isPending(entry.cpHex())) {
            externalProvider.requestLoad(entry.cpHex());
            return;
        }

        if (externalProvider == null) {
            LOG.warn("[ETM] externalProvider is NULL для {}", entry.cpHex()); // тимчасово
        }

        if (!IN_FLIGHT.add(entry.cpHex())) return;
        POOL.submit(() -> {
            try {
                byte[] png = fetchOrLoad(entry);
                if (png != null) uploadToGpu(entry, png);
            } catch (Exception ex) {
                LOG.warn("Failed emoji [{}]: {}", entry.cpHex(), ex.getMessage());
            } finally {
                IN_FLIGHT.remove(entry.cpHex());
            }
        });
    }

    private static final Set<String> failedThisSession = ConcurrentHashMap.newKeySet();

    private static byte[] fetchOrLoad(EmojiEntry entry) throws IOException, InterruptedException {
        Path file = cacheDir.resolve(entry.cpHex() + ".png");
        Path failFile = cacheDir.resolve(entry.cpHex() + ".404");

        if (Files.exists(file) && Files.size(file) > 0) {
            return Files.readAllBytes(file);
        }

        if (failedThisSession.contains(entry.cpHex()) || Files.exists(failFile)) {
            return null;
        }

        String url = String.format(CDN, entry.cpHex());
        HttpResponse<byte[]> resp = sendGet(url);

        if (resp.statusCode() == 404 && entry.cpHex().contains("_")) {
            String firstCp = entry.cpHex().split("_")[0];
            resp = sendGet(String.format(CDN, firstCp));
        }

        if (resp.statusCode() != 200) {
            LOG.debug("CDN {} for {}", resp.statusCode(), entry.cpHex());
            failedThisSession.add(entry.cpHex());
            try { Files.createFile(failFile); } catch (IOException ignored) {}
            return null;
        }

        byte[] bytes = resp.body();
        Path tmp = file.resolveSibling(entry.cpHex() + ".tmp");
        Files.write(tmp, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        return bytes;
    }

    private static HttpResponse<byte[]> sendGet(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .timeout(Duration.ofSeconds(12))
                .GET().build();
        return HTTP.send(req, HttpResponse.BodyHandlers.ofByteArray());
    }

    private static void uploadToGpu(EmojiEntry entry, byte[] png) {
        Minecraft.getInstance().execute(() -> {
            try {
                NativeImage img = NativeImage.read(new ByteArrayInputStream(png));
                DynamicTexture tex = new DynamicTexture(img);
                ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(
                        EmotionOverlays.MOD_ID, "emoji_dyn/" + entry.cpHex());
                Minecraft.getInstance().getTextureManager().register(loc, tex);
                LOADED.put(entry.cpHex(), loc);
            } catch (IOException e) {
                LOG.error("GPU upload failed for {}", entry.cpHex(), e);
            }
        });
    }

    public static void registerExternal(String cpHex, ResourceLocation loc) {
        LOADED.put(cpHex, loc);
    }
}
