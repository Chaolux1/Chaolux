package net.chaolux.chaolux.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import java.lang.reflect.Field;
import java.util.List;

public class ShaderController {
    private static final ResourceLocation SHADERS=new ResourceLocation("chaolux","shaders/post/chaolux.json");
    private static ShaderGroup shaderGroup;
    private static boolean enabled;
    public static void toggle() {
        if(enabled) {
            disable();
        } else {
            enable();
        }
        Minecraft minecraft=Minecraft.getMinecraft();
        if(minecraft.thePlayer != null) minecraft.thePlayer.addChatMessage(new ChatComponentText((enabled ? EnumChatFormatting.YELLOW : EnumChatFormatting.DARK_PURPLE) + StatCollector.translateToLocal(enabled ? "message.chaolux.shader_on" : "message.chaolux.shader_off")));
    }

    private static void enable() {
        Minecraft minecraft=Minecraft.getMinecraft();
        if(minecraft.theWorld == null || !OpenGlHelper.shadersSupported) return;
        try {
            minecraft.entityRenderer.activateNextShader();
            ShaderGroup group=minecraft.entityRenderer.getShaderGroup();
            ShaderGroup shader=new ShaderGroup(minecraft.getTextureManager(),minecraft.getResourceManager(),minecraft.getFramebuffer(),SHADERS);
            shader.createBindFramebuffers(minecraft.displayWidth,minecraft.displayHeight);
            minecraft.entityRenderer.theShaderGroup=shader;
            if(group != null) group.deleteShaderGroup();
            shaderGroup=shader;
            enabled=true;
            update();
        } catch (Exception exception) {
            disable();
        }
    }

    public static void disable() {
        Minecraft minecraft=Minecraft.getMinecraft();
        if(shaderGroup != null && minecraft.entityRenderer != null && minecraft.entityRenderer.getShaderGroup() == shaderGroup) minecraft.entityRenderer.deactivateShader();
        shaderGroup=null;
        enabled=false;
    }

    public static void update() {
        if(!enabled) return;
        Minecraft minecraft=Minecraft.getMinecraft();
        if(minecraft.theWorld == null) {
            disable();
            return;
        }
        if(minecraft.entityRenderer.getShaderGroup() != shaderGroup) {
            shaderGroup=null;
            enabled=false;
            return;
        }
        float day=(float)Math.sin(((minecraft.theWorld.getWorldTime() % 24000 + 24000) % 24000) / 24000.0D * Math.PI * 2);
        float time=Math.max(0.0f,Math.min(1.0f,(day + 0.18f) / 0.4f));
        time=time * time * (3.0f - 2.0f * time);
        try {
            for (Field field : ShaderGroup.class.getDeclaredFields()) {
                if(!List.class.isAssignableFrom(field.getType())) continue;
                field.setAccessible(true);
                Object object=field.get(shaderGroup);
                if(!(object instanceof List)) continue;
                for(Object objects : (List<?>)object) {
                    if(!(objects instanceof Shader)) continue;
                    ShaderUniform shaderUniform=((Shader)objects).getShaderManager().func_147991_a("Day");
                    if(shaderUniform != null) shaderUniform.func_148090_a(time);
                }
            }
        } catch (Exception exception) {

        }
    }
}
