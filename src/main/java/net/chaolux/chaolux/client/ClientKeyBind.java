package net.chaolux.chaolux.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class ClientKeyBind {
    public static final KeyMapping CAMERA=new KeyMapping("key.chaolux.camera", GLFW.GLFW_KEY_G,"key.categories.chaolux");
    public static final KeyMapping SHADER=new KeyMapping("key.chaolux.shader",GLFW.GLFW_KEY_H,"key.categories.chaolux");
    public static final KeyMapping MOVE_CAMERA=new KeyMapping("key.chaolux.move_camera",GLFW.GLFW_KEY_F7,"key.categories.chaolux");

}
