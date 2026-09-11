package net.chaolux.chaolux.registry.client;

import net.chaolux.chaolux.client.ClientKeyBind;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "chaolux", bus = Bus.MOD, value = Dist.CLIENT)
public class ModKeyEvents {
    @SubscribeEvent
    static void onRegisterKeyMapping(RegisterKeyMappingsEvent registerKeyMappingsEvent) {
        registerKeyMappingsEvent.register(ClientKeyBind.CAMERA);
        registerKeyMappingsEvent.register(ClientKeyBind.SHADER);
        registerKeyMappingsEvent.register(ClientKeyBind.MOVE_CAMERA);
    }
}
