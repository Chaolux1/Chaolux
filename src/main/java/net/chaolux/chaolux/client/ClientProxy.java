package net.chaolux.chaolux.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.chaolux.chaolux.client.command.CopyCommand;
import net.chaolux.chaolux.client.command.HideCommand;
import net.chaolux.chaolux.common.CommonProxy;
import net.chaolux.chaolux.registry.client.ModEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy {
    public static final KeyBinding CAMERA=new KeyBinding("key.chaolux.camera", Keyboard.KEY_G,"key.categories.chaolux");
    public static final KeyBinding SHADER=new KeyBinding("key.chaolux.shader", Keyboard.KEY_H,"key.categories.chaolux");
    public static final KeyBinding MOVE_CAMERA=new KeyBinding("key.chaolux.move_camera", Keyboard.KEY_F7,"key.categories.chaolux");

    @Override
    public void init() {
        ClientRegistry.registerKeyBinding(CAMERA);
        ClientRegistry.registerKeyBinding(SHADER);
        ClientRegistry.registerKeyBinding(MOVE_CAMERA);
        Minecraft minecraft=Minecraft.getMinecraft();
        if(minecraft.gameSettings.field_152396_an.getKeyCode() == Keyboard.KEY_F6) minecraft.gameSettings.setOptionKeyBinding(minecraft.gameSettings.field_152396_an,Keyboard.KEY_NONE);
        if(minecraft.gameSettings.field_152397_ao.getKeyCode() == Keyboard.KEY_F7) minecraft.gameSettings.setOptionKeyBinding(minecraft.gameSettings.field_152397_ao,Keyboard.KEY_NONE);
        minecraft.gameSettings.saveOptions();
        ModEvents modEvents=new ModEvents();
        FMLCommonHandler.instance().bus().register(modEvents);
        MinecraftForge.EVENT_BUS.register(modEvents);
        ClientCommandHandler.instance.registerCommand(new HideCommand());
        ClientCommandHandler.instance.registerCommand(new CopyCommand());
    }
}
