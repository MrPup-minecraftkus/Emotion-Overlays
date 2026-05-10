package com.mrpup.emotion_overlays.network;

import com.mrpup.emotion_overlays.EmotionOverlays;
import com.mrpup.emotion_overlays.common.EmojiData;
import com.mrpup.emotion_overlays.common.EmojiEntry;
import com.mrpup.emotion_overlays.common.EmojiRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record BroadcastEmojiPacket(UUID playerId, int emojiIndex) implements CustomPacketPayload {

    public static final Type<BroadcastEmojiPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(EmotionOverlays.MOD_ID, "broadcast_emoji"));

    public static final StreamCodec<ByteBuf, BroadcastEmojiPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
                    BroadcastEmojiPacket::playerId,
                    ByteBufCodecs.INT,
                    BroadcastEmojiPacket::emojiIndex,
                    BroadcastEmojiPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(BroadcastEmojiPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            EmojiEntry emoji = EmojiRegistry.byIndex(packet.emojiIndex());
            if (emoji != null) EmojiData.setEmoji(packet.playerId(), emoji);
        });
    }
}
