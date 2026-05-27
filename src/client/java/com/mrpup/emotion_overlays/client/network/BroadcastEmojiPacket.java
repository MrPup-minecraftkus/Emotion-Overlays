package com.mrpup.emotion_overlays.client.network;

import com.mrpup.emotion_overlays.EmotionOverlays;
import com.mrpup.emotion_overlays.common.EmojiData;
import com.mrpup.emotion_overlays.common.EmojiEntry;
import com.mrpup.emotion_overlays.common.EmojiRegistry;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record BroadcastEmojiPacket(UUID playerId, String emojiCpHex) implements CustomPacketPayload {

    public static final Type<BroadcastEmojiPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(EmotionOverlays.MOD_ID, "broadcast_emoji"));

    public static final StreamCodec<ByteBuf, BroadcastEmojiPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
                    BroadcastEmojiPacket::playerId,
                    ByteBufCodecs.STRING_UTF8,
                    BroadcastEmojiPacket::emojiCpHex,
                    BroadcastEmojiPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(BroadcastEmojiPacket packet, ClientPlayNetworking.Context context) {
        EmojiEntry emoji = EmojiRegistry.byCp(packet.emojiCpHex());
        if (emoji == null) {
            emoji = new EmojiEntry(
                    packet.emojiCpHex(),
                    packet.emojiCpHex(),
                    packet.emojiCpHex(),
                    "7TV"
            );
        }
        EmojiData.setEmoji(packet.playerId(), emoji);
    }
}