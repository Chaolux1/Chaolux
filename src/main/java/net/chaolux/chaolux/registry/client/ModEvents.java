package net.chaolux.chaolux.registry.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.chaolux.chaolux.client.ClientKeyBind;
import net.chaolux.chaolux.client.CommandHistory;
import net.chaolux.chaolux.client.command.HideCommand;
import net.chaolux.chaolux.client.gui.WeatherScreen;
import net.chaolux.chaolux.client.shader.ShaderController;
import net.chaolux.chaolux.common.event.MoveCameraEvent;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "chaolux", bus = Bus.FORGE, value = Dist.CLIENT)
public class ModEvents {
    private static boolean gWasPress;
    private static boolean gUsed;
    private static boolean upWasPress;
    private static boolean downWasPress;
    private static boolean rightWasPress;
    private static boolean leftWasPress;
    private static CameraType cameraType;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent clientTickEvent) {
        if(clientTickEvent.phase != TickEvent.Phase.END) return;
        Minecraft minecraft=Minecraft.getInstance();
        while (ClientKeyBind.SHADER.consumeClick()) {
            ShaderController.toggle();
        }
        MoveCameraEvent.tick(minecraft);
        if(minecraft.player == null || minecraft.screen != null) {
            reset(minecraft);
            gWasPress=ClientKeyBind.CAMERA.isDown();
            return;
        }
        long window=minecraft.getWindow().getWindow();
        boolean gPress=ClientKeyBind.CAMERA.isDown();
        boolean upPress= InputConstants.isKeyDown(window, GLFW.GLFW_KEY_UP);
        boolean downPress= InputConstants.isKeyDown(window, GLFW.GLFW_KEY_DOWN);
        boolean rightPress= InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT);
        boolean leftPress= InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT);
        if(gPress && !gWasPress) gUsed=false;
        if(gPress) {
            if(upPress && !upWasPress) {
                rotate(minecraft.player,0.0f,-5.0f);
                gUsed=true;
            }
            if(downPress && !downWasPress) {
                rotate(minecraft.player,0.0f,5.0f);
                gUsed=true;
            }
            if(rightPress && !rightWasPress) {
                rotate(minecraft.player,5.0f,0.0f);
                gUsed=true;
            }
            if(leftPress && !leftWasPress) {
                rotate(minecraft.player,-5.0f,0.0f);
                gUsed=true;
            }
        }
        if(!gPress && gWasPress && !gUsed) setRotate(minecraft.player,0.0f,0.0f);
        gWasPress=gPress;
        upWasPress=upPress;
        downWasPress=downPress;
        rightWasPress=rightPress;
        leftWasPress=leftPress;
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key key) {
        if(key.getAction() != GLFW.GLFW_PRESS) return;
        Minecraft minecraft=Minecraft.getInstance();
        if(minecraft.player == null || minecraft.getWindow() == null) return;
        if(key.getKey() == GLFW.GLFW_KEY_F5 && minecraft.screen == null) cameraType= minecraft.options.getCameraType();
        long window=minecraft.getWindow().getWindow();
        boolean f5Press=InputConstants.isKeyDown(window,GLFW.GLFW_KEY_F5);
        boolean f6Press=InputConstants.isKeyDown(window,GLFW.GLFW_KEY_F6);
        if((key.getKey() == GLFW.GLFW_KEY_F5 || key.getKey() == GLFW.GLFW_KEY_F6) && f5Press && f6Press && !(minecraft.screen instanceof WeatherScreen)) {
            while (minecraft.options.keyTogglePerspective.consumeClick()) {

            }
            if(cameraType != null) minecraft.options.setCameraType(cameraType);
            minecraft.setScreen(new WeatherScreen());
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        Minecraft minecraft=Minecraft.getInstance();
        ShaderController.disable();
        MoveCameraEvent.reset(minecraft);
        cameraType=null;
    }

    @SubscribeEvent
    public static void ocClosing(ScreenEvent.Closing closing) {
        if(closing.getScreen() instanceof ChatScreen) CommandHistory.recordHistory();
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn loggingIn) {
        CommandHistory.loadChat();
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles computeCameraAngles) {
        MoveCameraEvent.angles(computeCameraAngles);
        ShaderController.update();
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent renderHandEvent) {
        if (MoveCameraEvent.isCurrent()) {
            renderHandEvent.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered interactionKeyMappingTriggered) {
        if(!MoveCameraEvent.isCurrent()) return;
        interactionKeyMappingTriggered.setSwingHand(false);
        interactionKeyMappingTriggered.setCanceled(true);
    }

    @SubscribeEvent
    public static void registerClientCommand(RegisterClientCommandsEvent registerClientCommandsEvent) {
        HideCommand.register(registerClientCommandsEvent);
    }

    @SubscribeEvent
    public static void renderNameTag(RenderNameTagEvent renderNameTagEvent) {
        HideCommand.renderNameTag(renderNameTagEvent);
    }

    private static void rotate(LocalPlayer localPlayer,float yaw,float pitch) {
        float yawDelta= Mth.wrapDegrees(localPlayer.getYRot() + yaw);
        float pitchDelta=Mth.clamp(localPlayer.getXRot() + pitch,-90.0f,90.0f);
        setRotate(localPlayer,yawDelta,pitchDelta);
    }

    private static void setRotate(LocalPlayer localPlayer,float yaw,float pitch) {
        localPlayer.setYRot(yaw);
        localPlayer.setXRot(pitch);
        localPlayer.setYHeadRot(yaw);
        localPlayer.setYBodyRot(yaw);
    }

    private static void reset(Minecraft minecraft) {
        if(minecraft.getWindow() == null) return;
        long window=minecraft.getWindow().getWindow();
        upWasPress=InputConstants.isKeyDown(window,GLFW.GLFW_KEY_UP);
        downWasPress=InputConstants.isKeyDown(window,GLFW.GLFW_KEY_DOWN);
        rightWasPress=InputConstants.isKeyDown(window,GLFW.GLFW_KEY_RIGHT);
        leftWasPress=InputConstants.isKeyDown(window,GLFW.GLFW_KEY_LEFT);

    }
}
