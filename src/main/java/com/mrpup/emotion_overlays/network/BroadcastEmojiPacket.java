package com.mrpup.emotion_overlays.network;

import com.mrpup.emotion_overlays.EmotionOverlays;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record BroadcastEmojiPacket(UUID playerId, String emojiCpHex) implements CustomPacketPayload {

    public static final Type<BroadcastEmojiPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(EmotionOverlays.MOD_ID, "broadcast_emoji"));

    public static final StreamCodec<ByteBuf, BroadcastEmojiPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
                    BroadcastEmojiPacket::playerId,
                    ByteBufCodecs.STRING_UTF8,
                    BroadcastEmojiPacket::emojiCpHex,
                    BroadcastEmojiPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}