package com.mrpup.emotion_overlays.client.emoji;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public class EmojiRenderType {

    private static final LoadingCache<Identifier, RenderType> CACHE =
            CacheBuilder.newBuilder()
                    .build(CacheLoader.from(EmojiRenderType::create));

    public static RenderType get(Identifier texture) {
        return CACHE.getUnchecked(texture);
    }

    private static RenderType create(Identifier texture) {
        var pipeline = RenderPipelines.ENTITY_TRANSLUCENT;

        var setup = RenderSetup.builder(pipeline)
                .withTexture("Sampler0", texture)
                .useOverlay()
                .createRenderSetup();

        return RenderType.create("emoji_render", setup);
    }
}
