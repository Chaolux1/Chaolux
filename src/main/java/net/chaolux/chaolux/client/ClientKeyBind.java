package net.chaolux.chaolux.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class ClientKeyBind {
    public static final KeyMapping CAMERA=new KeyMapping("key.chaolux.camera", InputConstants.KEY_G,"key.categories.chaolux");
    public static final KeyMapping SHADER=new KeyMapping("key.chaolux.shader",InputConstants.KEY_H,"key.categories.chaolux");
    public static final KeyMapping MOVE_CAMERA=new KeyMapping("key.chaolux.move_camera",InputConstants.KEY_F7,"key.categories.chaolux");

}
