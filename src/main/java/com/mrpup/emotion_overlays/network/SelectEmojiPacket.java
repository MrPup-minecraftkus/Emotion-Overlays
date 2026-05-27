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
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectEmojiPacket(String emojiCpHex) implements CustomPacketPayload {

    public static final Type<SelectEmojiPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(EmotionOverlays.MOD_ID, "select_emoji"));

    public static final StreamCodec<ByteBuf, SelectEmojiPacket> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(SelectEmojiPacket::new, SelectEmojiPacket::emojiCpHex);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SelectEmojiPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            String cpHex = packet.emojiCpHex();
            if (cpHex == null || cpHex.isBlank()) return;

            EmojiEntry emoji = EmojiRegistry.byCp(cpHex);
            if (emoji == null) {
                emoji = new EmojiEntry(cpHex, cpHex, cpHex, "7TV");
            }

            EmojiData.setEmoji(serverPlayer.getUUID(), emoji);

            BroadcastEmojiPacket broadcast = new BroadcastEmojiPacket(
                    serverPlayer.getUUID(), cpHex);
            serverPlayer.serverLevel().players().forEach(p ->
                    PacketDistributor.sendToPlayer(p, broadcast));
        });
    }
}
