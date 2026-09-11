package net.chaolux.chaolux.registry.network;

import net.chaolux.chaolux.common.network.WeatherPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL_V="1.0";
    private static int packetId=0;
    public static final SimpleChannel INSTANCE= NetworkRegistry.newSimpleChannel(new ResourceLocation("chaolux","main"), () -> PROTOCOL_V,PROTOCOL_V::equals,PROTOCOL_V::equals);
    public static void register() {
        INSTANCE.registerMessage(packetId++, WeatherPacket.class,WeatherPacket::encode,WeatherPacket::decode,WeatherPacket::handle);
    }

}
