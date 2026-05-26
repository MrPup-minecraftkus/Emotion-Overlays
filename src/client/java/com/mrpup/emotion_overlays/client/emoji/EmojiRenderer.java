package com.mrpup.emotion_overlays.client.emoji;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mrpup.emotion_overlays.common.EmojiData;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class EmojiRenderer {

    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(EmojiRenderer::onRenderWorld);
    }

    private static double lerp(double a, double b, float t) {
        return a + (b - a) * t;
    }

    private static void onRenderWorld(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.gameRenderer == null) return;

        var camera = mc.gameRenderer.getMainCamera();
        var camPos = camera.getPosition();

        var ps = context.matrixStack();
        if (ps == null) return;

        float pt = context.tickCounter().getGameTimeDeltaPartialTick(true);

        for (Player player : mc.level.players()) {
            EmojiData.ActiveEmoji active = EmojiData.getEmoji(player.getUUID());
            if (active == null) continue;

            float alpha = active.getAlpha();
            if (alpha <= 0.01f) continue;

            ResourceLocation tex = EmojiTextureManager.getTexture(active.emoji());
            if (tex == null) continue;

            double px = lerp(player.xOld, player.getX(), pt) - camPos.x;
            double py = lerp(player.yOld, player.getY(), pt) - camPos.y;
            double pz = lerp(player.zOld, player.getZ(), pt) - camPos.z;

            ps.pushPose();

            ps.translate(px, py + player.getBbHeight() + 1.1, pz);
            ps.mulPose(camera.rotation());

            float s = active.getScale() * 0.05f;
            ps.scale(s, -s, s);

            float bounce = (float) Math.sin(System.currentTimeMillis() / 300.0) * 1.5f;
            ps.translate(0, bounce, 0);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, tex);
            RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
            RenderSystem.disableCull();

            BufferBuilder buf = Tesselator.getInstance().begin(
                    VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            float h = 8f;
            int a = (int)(alpha * 255);
            var m = ps.last().pose();

            buf.addVertex(m, -h, -h, 0).setUv(0, 0).setColor(255, 255, 255, a);
            buf.addVertex(m,  h, -h, 0).setUv(1, 0).setColor(255, 255, 255, a);
            buf.addVertex(m,  h,  h, 0).setUv(1, 1).setColor(255, 255, 255, a);
            buf.addVertex(m, -h,  h, 0).setUv(0, 1).setColor(255, 255, 255, a);

            BufferUploader.drawWithShader(buf.build());

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            ps.popPose();
        }
    }
}
