package net.chaolux.chaolux.registry.network;

import net.chaolux.chaolux.common.network.WeatherPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetwork {
    public static void register(RegisterPayloadHandlersEvent registerPayloadHandlersEvent) {
        PayloadRegistrar payloadRegistrar=registerPayloadHandlersEvent.registrar("1.0");
        payloadRegistrar.playToServer(WeatherPacket.TYPE,WeatherPacket.STREAM_CODEC,WeatherPacket::handle);
    }
}
