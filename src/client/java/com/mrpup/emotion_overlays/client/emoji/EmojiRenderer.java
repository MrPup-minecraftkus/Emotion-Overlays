package com.mrpup.emotion_overlays.client.emoji;

import com.mojang.blaze3d.vertex.*;
import com.mrpup.emotion_overlays.common.EmojiData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;

public class EmojiRenderer {

    public static void onSubmitCustomGeometry(SubmitNodeCollector event, PoseStack eventPoseStack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.gameRenderer == null) return;


        var camera = mc.gameRenderer.getMainCamera();
        var camPos = camera.position();
        Quaternionf camRotation = camera.rotation();

        float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        for (Player player : mc.level.players()) {

            EmojiData.ActiveEmoji active = EmojiData.getEmoji(player.getUUID());

            if (active == null) continue;

            float alpha = active.getAlpha();
            if (alpha <= 0.01f) continue;

            Identifier tex = EmojiTextureManager.getTexture(active.emoji());
            if (tex == null) continue;

            double px = lerp(player.xOld, player.getX(), pt) - camPos.x;
            double py = lerp(player.yOld, player.getY(), pt) - camPos.y;
            double pz = lerp(player.zOld, player.getZ(), pt) - camPos.z;

            float bounceY = (float) Math.sin(System.currentTimeMillis() / 300.0) * 0.075f;
            float scale = active.getScale() * 0.06f;
            int   argb = (int)(alpha * 255) << 24 | 0x00FFFFFF;

            var renderType = EmojiRenderType.get(tex);

            event.submitCustomGeometry(eventPoseStack, renderType, (pose, consumer) -> {
                PoseStack local = new PoseStack();
                local.translate(px, py + player.getBbHeight() + 0.9 + bounceY, pz);
                local.mulPose(camRotation);
                local.scale(scale, -scale, scale);

                drawQuad(local.last(), consumer, argb);
            });
        }
    }


    private static void drawQuad(PoseStack.Pose pose, VertexConsumer consumer, int argb) {
        float h = 8f;
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >>  8) & 0xFF;
        int b =  argb        & 0xFF;

        int bright = 15728880;

        consumer.addVertex(pose, -h, -h, 0).setColor(r, g, b, a).setUv(0, 0).setLight(bright).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose,  h, -h, 0).setColor(r, g, b, a).setUv(1, 0).setLight(bright).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose,  h,  h, 0).setColor(r, g, b, a).setUv(1, 1).setLight(bright).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, -h,  h, 0).setColor(r, g, b, a).setUv(0, 1).setLight(bright).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0, 1, 0);
    }

    private static double lerp(double a, double b, float t) {
        return a + (b - a) * t;
    }
}
