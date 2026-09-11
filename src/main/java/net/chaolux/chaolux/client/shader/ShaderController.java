package net.chaolux.chaolux.client.shader;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

public class ShaderController {
    private static final ResourceLocation SHADERS=new ResourceLocation("chaolux","shaders/post/chaolux.json");
    private static final Field FIELD= ObfuscationReflectionHelper.findField(PostChain.class,"f_110009_");
    private static boolean enable;
    private static PostChain postChain;
    public static boolean isEnable() {
        return enable;
    }
    public static void toggle() {
        Minecraft minecraft=Minecraft.getInstance();
        if(enable) {
            disable();
        } else {
            enable=true;
            load(minecraft);
            if(postChain == null) enable=false;
        }
        if(minecraft.player != null) {
            int color=enable ? 0xFFDF2F : 0x9A00CC;
            minecraft.player.displayClientMessage(Component.translatable(enable ? "message.chaolux.shader_on" : "message.chaolux.shader_off").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))),true);
        }
    }

    public static void disable() {
        Minecraft minecraft=Minecraft.getInstance();
        enable=false;
        postChain=null;
        minecraft.gameRenderer.shutdownEffect();
        minecraft.gameRenderer.checkEntityPostEffect(minecraft.getCameraEntity());
    }

    public static void update() {
        if(!enable) return;
        Minecraft minecraft=Minecraft.getInstance();
        if(minecraft.level == null) return;
        if(minecraft.gameRenderer.currentEffect() != postChain) load(minecraft);
        if(postChain == null) return;
        setDay(postChain,getDay(minecraft));
    }

    private static void load(Minecraft minecraft) {
        minecraft.gameRenderer.loadEffect(SHADERS);
        postChain=minecraft.gameRenderer.currentEffect();
    }

    private static float getDay(Minecraft minecraft) {
        long time=Math.floorMod(minecraft.level.getDayTime(),24000);
        float day=(float) Math.sin(time / 24000.0 * Math.PI * 2);
        return smooth(-0.18f,0.22f,day);
    }

    private static float smooth(float smooth,float step,float value) {
        float x=Math.max(0.0f,Math.min(1.0f,(value - smooth) / (step - smooth)));
        return x * x * (3.0f - 2.0f * x);
    }

    @SuppressWarnings("unchecked")
    private static void setDay(PostChain postChain,float value) {
        try {
            List<PostPass> postPassList=(List<PostPass>) FIELD.get(postChain);
            for (PostPass postPass : postPassList) {
                Uniform uniform=postPass.getEffect().getUniform("Day");
                if(uniform != null) uniform.set(value);
            }
        } catch (IllegalAccessException exception) {

        }
    }
}
