package com.mrpup.emotion_overlays.client;

import com.mrpup.emotion_overlays.EmotionOverlays;
import com.mrpup.emotion_overlays.common.EmojiData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = EmotionOverlays.MOD_ID, value = Dist.CLIENT)
public class EmojiRenderer {

    private static double lerp(double a, double b, float t) {
        return a + (b - a) * t;
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.gameRenderer == null) return;

        var camera = mc.gameRenderer.getMainCamera();
        var camPos = camera.getPosition();

        var ps = event.getPoseStack();



        for (Player player : mc.level.players()) {
            EmojiData.ActiveEmoji active = EmojiData.getEmoji(player.getUUID());
            if (active == null) continue;

            float alpha = active.getAlpha();
            if (alpha <= 0.01f) continue;

            ResourceLocation tex = EmojiTextureManager.getTexture(active.emoji());
            if (tex == null) continue;

            float pt = event.getPartialTick().getGameTimeDeltaPartialTick(true);
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
