package net.chaolux.chaolux.registry.client;

import net.chaolux.chaolux.client.ClientKeyBind;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = "chaolux", value = Dist.CLIENT)
public class ModKeyEvents {
    @SubscribeEvent
    static void onRegisterKeyMapping(RegisterKeyMappingsEvent registerKeyMappingsEvent) {
        registerKeyMappingsEvent.register(ClientKeyBind.CAMERA);
        registerKeyMappingsEvent.register(ClientKeyBind.SHADER);
        registerKeyMappingsEvent.register(ClientKeyBind.MOVE_CAMERA);
    }
}
