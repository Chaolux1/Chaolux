package net.chaolux.chaolux.registry.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.chaolux.chaolux.client.ClientProxy;
import net.chaolux.chaolux.client.CommandHistory;
import net.chaolux.chaolux.client.command.CopyCommand;
import net.chaolux.chaolux.client.command.HideCommand;
import net.chaolux.chaolux.client.gui.SwitcherScreen;
import net.chaolux.chaolux.client.gui.Type;
import net.chaolux.chaolux.client.shader.ShaderController;
import net.chaolux.chaolux.common.event.MoveCameraEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import org.lwjgl.input.Keyboard;

public class ModEvents {
    private static boolean gWasPress;
    private static boolean gUsed;
    private static boolean upWasPress;
    private static boolean downWasPress;
    private static boolean rightWasPress;
    private static boolean leftWasPress;
    private WorldClient worldClient;
    private static boolean history;
    private static boolean weatherWasPress;
    private static boolean modeWasPress;
    private static boolean debug;
    private static int save;

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if(clientTickEvent.phase != TickEvent.Phase.END) return;
        Minecraft minecraft=Minecraft.getMinecraft();
        if(minecraft.theWorld != worldClient) {
            MoveCameraEvent.reset(minecraft);
            ShaderController.disable();
            CommandHistory.save();
            CopyCommand.clear();
            HideCommand.clear();
            history=false;
            SwitcherScreen.resetMode();
            worldClient=minecraft.theWorld;
        }
        if(minecraft.thePlayer == null) return;
        if(!history) {
            CommandHistory.load();
            history=true;
        }
        CommandHistory.recordHistory();
        while (ClientProxy.SHADER.isPressed()) {
            ShaderController.toggle();
        }
        ShaderController.update();
        MoveCameraEvent.tick(minecraft);
        camera(minecraft);
        boolean f3= Keyboard.isKeyDown(Keyboard.KEY_F3);
        boolean f4= Keyboard.isKeyDown(Keyboard.KEY_F4);
        boolean f5= Keyboard.isKeyDown(Keyboard.KEY_F5);
        boolean f6= Keyboard.isKeyDown(Keyboard.KEY_F6);
        boolean weather=f5 && f6;
        boolean mode=f3 && f4;
        if(minecraft.currentScreen == null) {
            if(!f3) debug=minecraft.gameSettings.showDebugInfo;
            if(!f5) save=minecraft.gameSettings.thirdPersonView;
        }
        if(minecraft.currentScreen == null && !modeWasPress && mode) {
            minecraft.gameSettings.showDebugInfo=debug;
            minecraft.displayGuiScreen(new SwitcherScreen(Type.GAMEMODE));
        } else if(minecraft.currentScreen == null && !weatherWasPress && weather) {
            minecraft.gameSettings.thirdPersonView=save;
            minecraft.displayGuiScreen(new SwitcherScreen(Type.WEATHER));
        }
        if(minecraft.currentScreen instanceof SwitcherScreen) {
            if(((SwitcherScreen)minecraft.currentScreen).getType() == Type.WEATHER) {
                minecraft.gameSettings.thirdPersonView=save;
            } else {
                minecraft.gameSettings.showDebugInfo=debug;
            }
        }
        modeWasPress=mode;
        weatherWasPress=weather;
    }

    @SubscribeEvent
    public void renderNameTag(RenderLivingEvent.Specials.Pre pre) {
        if(pre.entity instanceof EntityPlayer && HideCommand.isHidden(pre.entity.getCommandSenderName())) pre.setCanceled(true);
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent renderHandEvent) {
        if(MoveCameraEvent.isCurrent()) renderHandEvent.setCanceled(true);
    }

    @SubscribeEvent
    public void onInteractionKey(MouseEvent mouseEvent) {
        if(MoveCameraEvent.isCurrent() && mouseEvent.button >= 0) mouseEvent.setCanceled(true);
    }

    private void camera(Minecraft minecraft) {
        boolean gPress=ClientProxy.CAMERA.getIsKeyPressed();
        boolean upPress=Keyboard.isKeyDown(Keyboard.KEY_UP);
        boolean downPress=Keyboard.isKeyDown(Keyboard.KEY_DOWN);
        boolean leftPress=Keyboard.isKeyDown(Keyboard.KEY_LEFT);
        boolean rightPress=Keyboard.isKeyDown(Keyboard.KEY_RIGHT);
        if(minecraft.currentScreen == null && !MoveCameraEvent.isCurrent()) {
            if(gPress && !gWasPress) gUsed=false;
            if(gPress) {
                if (upPress && !upWasPress) {
                    rotate(minecraft, 0.0f, -5.0f);
                    gUsed = true;
                }
                if (downPress && !downWasPress) {
                    rotate(minecraft, 0.0f, 5.0f);
                    gUsed = true;
                }
                if (leftPress && !leftWasPress) {
                    rotate(minecraft,-5.0f,0.0f);
                    gUsed=true;
                }
                if(rightPress && !rightWasPress) {
                    rotate(minecraft,5.0f,0.0f);
                    gUsed=true;
                }
            }
            if(!gPress && gWasPress && !gUsed) setRotate(minecraft);
        }
        gWasPress=gPress;
        upWasPress=upPress;
        downWasPress=downPress;
        leftWasPress=leftPress;
        rightWasPress=rightPress;
    }

    private void setRotate(Minecraft minecraft) {
        minecraft.thePlayer.rotationYaw=0;
        minecraft.thePlayer.rotationPitch=0;
        minecraft.thePlayer.rotationYawHead=0;
        minecraft.thePlayer.renderYawOffset=0;
    }

    private void rotate(Minecraft minecraft,float yaw,float pitch) {
        minecraft.thePlayer.rotationYaw= MathHelper.wrapAngleTo180_float(minecraft.thePlayer.rotationYaw + yaw);
        minecraft.thePlayer.rotationPitch=MathHelper.clamp_float(minecraft.thePlayer.rotationPitch + pitch,-90.0f,90.0f);
        minecraft.thePlayer.rotationYawHead=minecraft.thePlayer.rotationYaw;
        minecraft.thePlayer.renderYawOffset=minecraft.thePlayer.rotationYaw;
    }
}
