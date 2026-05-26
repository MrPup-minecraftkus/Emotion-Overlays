package com.mrpup.emotion_overlays.client.network;

import com.mrpup.emotion_overlays.EmotionOverlays;
import com.mrpup.emotion_overlays.common.EmojiData;
import com.mrpup.emotion_overlays.common.EmojiEntry;
import com.mrpup.emotion_overlays.common.EmojiRegistry;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record SelectEmojiPacket(int emojiIndex) implements CustomPacketPayload {

    public static final Type<SelectEmojiPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(EmotionOverlays.MOD_ID, "select_emoji"));

    public static final StreamCodec<ByteBuf, SelectEmojiPacket> STREAM_CODEC =
            ByteBufCodecs.INT.map(SelectEmojiPacket::new, SelectEmojiPacket::emojiIndex);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SelectEmojiPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayer serverPlayer = context.player();
        EmojiEntry emoji = EmojiRegistry.byIndex(packet.emojiIndex());
        if (emoji == null) return;

        EmojiData.setEmoji(serverPlayer.getUUID(), emoji);

        BroadcastEmojiPacket broadcast = new BroadcastEmojiPacket(
                serverPlayer.getUUID(), packet.emojiIndex());

        serverPlayer.serverLevel().players().forEach(p ->
                ServerPlayNetworking.send(p, broadcast));
    }
}
