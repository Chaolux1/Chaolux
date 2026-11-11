package net.chaolux.chaolux.common.event;

import com.mojang.authlib.GameProfile;
import net.chaolux.chaolux.client.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

public class MoveCameraEvent {
    private static boolean current;
    private static boolean move;
    private static boolean f7WasPress;
    private static boolean upWasPress;
    private static boolean downWasPress;
    private static boolean rightWasPress;
    private static boolean leftWasPress;
    private static boolean shiftWasPress;
    private static boolean ctrlWasPress;
    private static float yaw;
    private static float cameraYaw;
    private static float headYaw;
    private static float bodyYaw;
    private static float pitch;
    private static float cameraPitch;
    private static WorldClient worldClient;
    private static EntityOtherPlayerMP entityOtherPlayerMP;
    private static int value;

    public static void tick(Minecraft minecraft) {
        if (minecraft.theWorld == null || minecraft.thePlayer == null) {
            reset(minecraft);
            return;
        }
        if (worldClient != null && worldClient != minecraft.theWorld) reset(minecraft);
        boolean f7Press = ClientProxy.MOVE_CAMERA.getIsKeyPressed();
        boolean upPress = Keyboard.isKeyDown(Keyboard.KEY_UP), downPress = Keyboard.isKeyDown(Keyboard.KEY_DOWN);
        boolean leftPress = Keyboard.isKeyDown(Keyboard.KEY_LEFT), rightPress = Keyboard.isKeyDown(Keyboard.KEY_RIGHT);
        boolean shiftPress = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT), ctrlPress = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
        if (minecraft.currentScreen != null) {
            sync(f7Press, upPress, downPress, leftPress, rightPress, shiftPress, ctrlPress);
            return;
        }
        if (f7Press && !f7WasPress) move = false;
        if (f7Press) {
            if (upPress && !upWasPress) {
                moveCamera(minecraft,1, 0, 0);
                move = true;
            }
            if (downPress && !downWasPress) {
                moveCamera(minecraft,-1, 0, 0);
                move = true;
            }
            if (rightPress && !rightWasPress) {
                moveCamera(minecraft,0, 1, 0);
                move = true;
            }
            if (leftPress && !leftWasPress) {
                moveCamera(minecraft,0, -1, 0);
                move = true;
            }
            if (shiftPress && !shiftWasPress) {
                moveCamera(minecraft,0, 0, -1);
                move = true;
            }
            if (ctrlPress && !ctrlWasPress) {
                moveCamera(minecraft,0, 0, 1);
                move = true;
            }
        }
        if (!f7Press && f7WasPress && !move) {
            if (current) {
                disable(minecraft);
            } else {
                enable(minecraft);
            }
        }
        if (current && entityOtherPlayerMP != null) {
            EntityClientPlayerMP entityClientPlayerMP = minecraft.thePlayer;
            if (f7Press) {
                float mouseYaw = MathHelper.wrapAngleTo180_float(entityClientPlayerMP.rotationYaw - cameraYaw);
                float mousePitch = entityClientPlayerMP.rotationPitch - cameraPitch;
                entityOtherPlayerMP.rotationYaw = MathHelper.wrapAngleTo180_float(entityOtherPlayerMP.rotationYaw + mouseYaw);
                entityOtherPlayerMP.rotationPitch = MathHelper.clamp_float(entityOtherPlayerMP.rotationPitch + mousePitch, -90.0f, 90.0f);
                move |= Math.abs(mouseYaw) > 0.001f || Math.abs(mousePitch) > 0.001f;
            }
            entityClientPlayerMP.rotationYaw = yaw;
            entityClientPlayerMP.rotationPitch = pitch;
            entityClientPlayerMP.rotationYawHead = headYaw;
            entityClientPlayerMP.renderYawOffset = bodyYaw;
            entityOtherPlayerMP.prevRotationYaw = entityOtherPlayerMP.rotationYaw;
            entityOtherPlayerMP.prevRotationPitch = entityOtherPlayerMP.rotationPitch;
            minecraft.renderViewEntity = entityOtherPlayerMP;
            minecraft.gameSettings.thirdPersonView = 0;
            cameraYaw = entityClientPlayerMP.rotationYaw;
            cameraPitch = entityClientPlayerMP.rotationPitch;
            if (f7Press) {
                KeyBinding.setKeyBindState(minecraft.gameSettings.keyBindSneak.getKeyCode(), false);
                KeyBinding.setKeyBindState(minecraft.gameSettings.keyBindSprint.getKeyCode(), false);
            }
        }
        sync(f7Press,upPress,downPress,leftPress,rightPress,shiftPress,ctrlPress);
    }

    public static boolean isCurrent() {
        return current;
    }

    public static void reset(Minecraft minecraft) {
        if(current) disable(minecraft);
        entityOtherPlayerMP=null;
        worldClient=null;
        current=move=f7WasPress=upWasPress=downWasPress=leftWasPress=rightWasPress=shiftWasPress=ctrlWasPress=false;
    }

    private static void enable(Minecraft minecraft) {
        if(minecraft.theWorld == null || minecraft.thePlayer == null) return;
        if(current) return;
        worldClient=minecraft.theWorld;
        EntityClientPlayerMP entityClientPlayerMP=minecraft.thePlayer;
        yaw=entityClientPlayerMP.rotationYaw;
        pitch=entityClientPlayerMP.rotationPitch;
        headYaw=entityClientPlayerMP.rotationYawHead;
        bodyYaw=entityClientPlayerMP.renderYawOffset;
        cameraYaw=entityClientPlayerMP.rotationYaw;
        cameraPitch=entityClientPlayerMP.rotationPitch;
        entityOtherPlayerMP=new EntityOtherPlayerMP(worldClient,new GameProfile(entityClientPlayerMP.getUniqueID(),entityClientPlayerMP.getCommandSenderName()));
        entityOtherPlayerMP.setPositionAndRotation(entityClientPlayerMP.posX,entityClientPlayerMP.posY,entityClientPlayerMP.posZ,entityClientPlayerMP.rotationYaw,entityClientPlayerMP.rotationPitch);
        syncCamera();
        entityOtherPlayerMP.prevRotationYaw=entityOtherPlayerMP.rotationYaw;
        entityOtherPlayerMP.prevRotationPitch=entityOtherPlayerMP.rotationPitch;
        value=minecraft.gameSettings.thirdPersonView;
        minecraft.gameSettings.thirdPersonView=0;
        minecraft.renderViewEntity=entityOtherPlayerMP;
        current=true;
    }

    private static void disable(Minecraft minecraft) {
        current=false;
        if(minecraft.thePlayer != null) {
            minecraft.renderViewEntity=minecraft.thePlayer;
            minecraft.gameSettings.thirdPersonView=value;
            minecraft.thePlayer.rotationYaw=yaw;
            minecraft.thePlayer.rotationPitch=pitch;
            minecraft.thePlayer.rotationYawHead=headYaw;
            minecraft.thePlayer.renderYawOffset=bodyYaw;
        }
        entityOtherPlayerMP=null;
    }

    private static void moveCamera(Minecraft minecraft,double up,double right,double vertical) {
        if(!current) enable(minecraft);
        if(entityOtherPlayerMP == null) return;
        double angle=Math.toRadians(entityOtherPlayerMP.rotationYaw);
        double x= -Math.sin(angle) * up + Math.cos(angle) * right;
        double z=Math.cos(angle) * up + Math.sin(angle) * right;
        entityOtherPlayerMP.setPosition(entityOtherPlayerMP.posX + x,entityOtherPlayerMP.posY + vertical,entityOtherPlayerMP.posZ + z);
        syncCamera();
    }

    private static void sync(boolean f7Press,boolean upPress,boolean downPress,boolean rightPress,boolean leftPress,boolean shiftPress,boolean ctrlPress) {
        f7WasPress=f7Press;
        upWasPress=upPress;
        downWasPress=downPress;
        rightWasPress=rightPress;
        leftWasPress=leftPress;
        shiftWasPress=shiftPress;
        ctrlWasPress=ctrlPress;
    }

    private static void syncCamera() {
        if(entityOtherPlayerMP == null) return;
        entityOtherPlayerMP.prevPosX=entityOtherPlayerMP.posX;
        entityOtherPlayerMP.prevPosY=entityOtherPlayerMP.posY;
        entityOtherPlayerMP.prevPosZ=entityOtherPlayerMP.posZ;
        entityOtherPlayerMP.lastTickPosX=entityOtherPlayerMP.posX;
        entityOtherPlayerMP.lastTickPosY=entityOtherPlayerMP.posY;
        entityOtherPlayerMP.lastTickPosZ=entityOtherPlayerMP.posZ;
    }
}
