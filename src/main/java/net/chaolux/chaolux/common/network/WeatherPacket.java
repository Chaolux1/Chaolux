package net.chaolux.chaolux.common.network;

import io.netty.buffer.ByteBuf;
import net.chaolux.chaolux.chaolux;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public record WeatherPacket(Action action,int customTime) implements CustomPacketPayload {
    private static final int DURATION=20 * 60 * 10;
    public static final Type<WeatherPacket> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("chaolux","weather"));
    public static final StreamCodec<ByteBuf,WeatherPacket> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT, weatherPacket -> weatherPacket.action().ordinal(),ByteBufCodecs.VAR_INT,WeatherPacket::customTime,(ordinal,weather) -> new WeatherPacket(ordinal >= 0 && ordinal < Action.values().length ? Action.values()[ordinal] : Action.DAY,weather));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WeatherPacket weatherPacket, IPayloadContext iPayloadContext) {
        if(!(iPayloadContext.player() instanceof ServerPlayer serverPlayer)) return;
            if(!serverPlayer.hasPermissions(2)) {
                serverPlayer.sendSystemMessage(Component.translatable("permissions.requires.player").withStyle(ChatFormatting.RED));
                return;
            }
            switch (weatherPacket.action) {
                case DAY -> setTime(serverPlayer,1000);
                case NIGHT -> setTime(serverPlayer,13000);
                case RAIN -> serverPlayer.serverLevel().setWeatherParameters(0,DURATION,true,false);
                case THUNDER -> serverPlayer.serverLevel().setWeatherParameters(0,DURATION,true,true);
                case CLEAR -> serverPlayer.serverLevel().setWeatherParameters(DURATION,0,false,false);
                case CUSTOM_TIME -> {
                    int time=Math.max(0,Math.min(23,weatherPacket.customTime));
                    long customTime=Math.floorMod((time - 6) * 1000,24000);
                    setTime(serverPlayer,customTime);
                }
            }
    }

    private static void setTime(ServerPlayer serverPlayer,long customTime) {
        for (ServerLevel serverLevel : serverPlayer.serverLevel().getServer().getAllLevels()) {
            long currentTime=serverLevel.getDayTime() - Math.floorMod(serverLevel.getDayTime(),24000);
            serverLevel.setDayTime(currentTime + customTime);
        }
    }
}
