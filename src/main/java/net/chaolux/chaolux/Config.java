package net.chaolux.chaolux;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = chaolux.MOD_ID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER;
    static final ModConfigSpec SPEC;

    static {
        BUILDER = new ModConfigSpec.Builder();
        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {

    }
}
