package net.chaolux.chaolux.common.network;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WeatherPacket {
    private static final int DURATION=20 * 60 * 10;
    private final Action action;
    private final int customTime;
    public WeatherPacket(Action action,int customTime) {
        this.action=action;
        this.customTime=customTime;
    }

    public static void encode(WeatherPacket weatherPacket, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(weatherPacket.action.ordinal());
        friendlyByteBuf.writeVarInt(weatherPacket.customTime);
    }

    public static WeatherPacket decode(FriendlyByteBuf friendlyByteBuf) {
        int data=friendlyByteBuf.readVarInt();
        int time=friendlyByteBuf.readVarInt();
        Action[] actions=Action.values();
        Action action=data >= 0 && data < actions.length ? actions[data] : Action.DAY;
        return new WeatherPacket(action,time);
    }

    public static void handle(WeatherPacket weatherPacket, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context=contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer=context.getSender();
            if(serverPlayer == null) return;
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
        });
        context.setPacketHandled(true);
    }

    private static void setTime(ServerPlayer serverPlayer,long customTime) {
        for (ServerLevel serverLevel : serverPlayer.serverLevel().getServer().getAllLevels()) {
            long currentTime=serverLevel.getDayTime() - Math.floorMod(serverLevel.getDayTime(),24000);
            serverLevel.setDayTime(currentTime + customTime);
        }
    }
}
