package com.mrpup.emotion_overlays.client.network;

import com.mrpup.emotion_overlays.EmotionOverlays;
import com.mrpup.emotion_overlays.common.EmojiData;
import com.mrpup.emotion_overlays.common.EmojiEntry;
import com.mrpup.emotion_overlays.common.EmojiRegistry;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record SelectEmojiPacket(String emojiCpHex) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SelectEmojiPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(EmotionOverlays.MOD_ID, "select_emoji"));

    public static final StreamCodec<ByteBuf, SelectEmojiPacket> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(SelectEmojiPacket::new, SelectEmojiPacket::emojiCpHex);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SelectEmojiPacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayer serverPlayer = context.player();
            EmojiEntry emoji = EmojiRegistry.byCp(payload.emojiCpHex());
            if (emoji == null) return;
            EmojiData.setEmoji(serverPlayer.getUUID(), emoji);
            BroadcastEmojiPacket broadcast = new BroadcastEmojiPacket(
                    serverPlayer.getUUID(), payload.emojiCpHex());
            PlayerLookup.all(context.server()).forEach(p ->
                    ServerPlayNetworking.send(p, broadcast));
        });
    }
}
