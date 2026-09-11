package net.chaolux.chaolux.common.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.chaolux.chaolux.client.ClientKeyBind;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent;
import org.lwjgl.glfw.GLFW;

public class MoveCameraEvent {
    private static final double MOVE=1;
    private static Vec3 vec3;
    private static ClientLevel clientLevel;
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
    private static CameraType cameraType;
    public static void tick(Minecraft minecraft) {
        if(minecraft.player == null || minecraft.level == null || minecraft.getWindow() == null) {
            reset(minecraft);
            return;
        }
        if(clientLevel != null && clientLevel != minecraft.level) reset(minecraft);
        long window=minecraft.getWindow().getWindow();
        boolean f7Press= ClientKeyBind.MOVE_CAMERA.isDown();
        boolean upPress= InputConstants.isKeyDown(window, GLFW.GLFW_KEY_UP);
        boolean downPress= InputConstants.isKeyDown(window, GLFW.GLFW_KEY_DOWN);
        boolean rightPress= InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT);
        boolean leftPress= InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT);
        boolean shiftPress= InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT);
        boolean ctrlPress= InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL);
        if(minecraft.screen != null) {
            sync(f7Press,upPress,downPress,rightPress,leftPress,shiftPress,ctrlPress);
            return;
        }
        if(f7Press && !f7WasPress) {
            move=false;
            cameraYaw=minecraft.player.getYRot();
            cameraPitch=minecraft.player.getXRot();
            headYaw=minecraft.player.getYHeadRot();
            bodyYaw=minecraft.player.yBodyRot;
        }
        if(f7Press) {
            minecraft.options.keyShift.setDown(false);
            minecraft.options.keySprint.setDown(false);
            if(upPress && !upWasPress) {
                moveCamera(minecraft,MOVE,0.0,0.0);
                move=true;
            }
            if(downPress && !downWasPress) {
                moveCamera(minecraft,-MOVE,0.0,0.0);
                move=true;
            }
            if(leftPress && !leftWasPress) {
                moveCamera(minecraft,0.0,-MOVE,0.0);
                move=true;
            }
            if(rightPress && !rightWasPress) {
                moveCamera(minecraft,0.0,MOVE,0.0);
                move=true;
            }
            if(shiftPress && !shiftWasPress) {
                moveCamera(minecraft,0.0,0.0,-MOVE);
                move=true;
            }
            if(ctrlPress && !ctrlWasPress) {
                moveCamera(minecraft,0.0,0.0,MOVE);
                move=true;
            }
        }
        if(!f7Press && f7WasPress && !move) toggle(minecraft);
        sync(f7Press,upPress,downPress,rightPress,leftPress,shiftPress,ctrlPress);
    }

    private static void moveCamera(Minecraft minecraft,double up,double right,double vertical) {
        provide(minecraft);
        if(vec3 == null) return;
        if(!current) {
            enable(minecraft);
            if(!current) return;
        }
        Vec3 upVec=Vec3.directionFromRotation(0.0f,yaw);
        Vec3 rightVec=new Vec3(-upVec.z,0.0,upVec.x);
        Vec3 verticalVec=upVec.scale(up).add(rightVec.scale(right)).add(0.0,vertical,0.0);
        upVec=new Vec3(upVec.x,0.0,upVec.z).normalize();
        vec3=vec3.add(verticalVec);
    }

    public static void angles(ViewportEvent.ComputeCameraAngles computeCameraAngles) {
        if(!current || vec3 == null) return;
        Minecraft minecraft=Minecraft.getInstance();
        LocalPlayer localPlayer=minecraft.player;
        boolean f7Press=ClientKeyBind.MOVE_CAMERA.isDown();
        if(localPlayer == null) return;
        if(f7Press) {
            float angleYaw = Mth.wrapDegrees(localPlayer.getYRot() - cameraYaw);
            float anglePitch = localPlayer.getXRot() - cameraPitch;
            if (Math.abs(angleYaw) > 0.0001f || Math.abs(anglePitch) > 0.0001) {
                yaw = Mth.wrapDegrees(yaw + angleYaw);
                pitch = Mth.clamp(pitch + anglePitch, -90.0f, 90.0f);
                move = true;
            }
            restoreRotate(localPlayer);
        }
        Camera camera=computeCameraAngles.getCamera();
        camera.setPosition(vec3.x,vec3.y,vec3.z);
        computeCameraAngles.setYaw(yaw);
        computeCameraAngles.setPitch(pitch);
        computeCameraAngles.setRoll(0.0f);
    }

    public static boolean isCurrent() {
        return current;
    }

    public static void reset(Minecraft minecraft) {
        if(current) disable(minecraft);
        vec3=null;
        clientLevel=null;
        current=false;
        f7WasPress=false;
        move=false;
        upWasPress=false;
        downWasPress=false;
        rightWasPress=false;
        leftWasPress=false;
        shiftWasPress=false;
        ctrlWasPress=false;
        cameraType=null;
    }

    private static void toggle(Minecraft minecraft) {
        if(current) {
            disable(minecraft);
            return;
        }
        provide(minecraft);
        if(vec3 == null) return;
        enable(minecraft);
    }

    private static void disable(Minecraft minecraft) {
        if(cameraType != null) minecraft.options.setCameraType(cameraType);
        current=false;
    }

    private static void enable(Minecraft minecraft) {
        if(minecraft.player == null) return;
        provide(minecraft);
        if(vec3 == null) return;
        cameraType=minecraft.options.getCameraType();
        minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        current=true;
    }

    private static void provide(Minecraft minecraft) {
        if(vec3 != null && clientLevel == minecraft.level) return;
        if(minecraft.level == null) return;
        Camera camera=minecraft.gameRenderer.getMainCamera();
        vec3=camera.getPosition();
        yaw=camera.getYRot();
        pitch=camera.getXRot();
        clientLevel=minecraft.level;
    }

    private static void restoreRotate(LocalPlayer localPlayer) {
        localPlayer.setYRot(cameraYaw);
        localPlayer.setXRot(cameraPitch);
        localPlayer.setYHeadRot(headYaw);
        localPlayer.setYBodyRot(bodyYaw);
        localPlayer.yRotO=cameraYaw;
        localPlayer.xRotO=cameraPitch;
        localPlayer.yHeadRotO=headYaw;
        localPlayer.yBodyRotO=bodyYaw;
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
}
