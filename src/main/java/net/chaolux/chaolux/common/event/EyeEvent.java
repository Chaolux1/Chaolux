package net.chaolux.chaolux.common.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.chaolux.chaolux.Chaolux;
import net.chaolux.chaolux.registry.network.ModNetwork;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class EyeEvent {
    @SubscribeEvent
    public void onEntityInteract(EntityInteractEvent entityInteractEvent) {
        if(entityInteractEvent.entityPlayer.getHeldItem() == null || entityInteractEvent.entityPlayer.getHeldItem().getItem() != Chaolux.eyeItem) return;
        entityInteractEvent.setCanceled(true);
        if(entityInteractEvent.entityPlayer.worldObj.isRemote) return;
        NBTTagCompound nbtTagCompound=new NBTTagCompound();
        entityInteractEvent.target.writeToNBT(nbtTagCompound);
        copyTag(entityInteractEvent.entityPlayer,"Entity" + entityInteractEvent.target.getCommandSenderName(),nbtTagCompound);
    }

    private static IChatComponent currentTag(String string, EnumChatFormatting enumChatFormatting) {
        return new ChatComponentText(string).setChatStyle(new ChatStyle().setColor(enumChatFormatting));
    }

    public static void getNoBlockEntity(EntityPlayer entityPlayer,int x,int y,int z) {
        entityPlayer.addChatMessage(currentTag("[EYE] No BlockEntity " + x + " " + y + " " + z,EnumChatFormatting.GRAY));
    }

    public static void copyTag(EntityPlayer entityPlayer,String string,NBTTagCompound nbtTagCompound) {
        IChatComponent iChatComponent=currentTag("[EYE] ",EnumChatFormatting.DARK_PURPLE);
        iChatComponent.appendSibling(currentTag(string + " ",EnumChatFormatting.WHITE));
        if(entityPlayer instanceof EntityPlayerMP) {
            int value= ModNetwork.copy((EntityPlayerMP)entityPlayer,nbtTagCompound.toString());
            if(value >= 0) {
                IChatComponent chatComponent=currentTag("[COPY]",EnumChatFormatting.YELLOW);
                chatComponent.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW).setBold(true).setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/chaoluxcopy " + value)).setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ChatComponentText("Copy full NBT"))));
                iChatComponent.appendSibling(chatComponent);
            }
        }
        entityPlayer.addChatMessage(iChatComponent);
        send(entityPlayer,nbtTagCompound,0);
    }

    private static String indexTag(int value) {
        StringBuilder stringBuilder=new StringBuilder();
        for (int index=0;index < Math.min(value,16);index++) {
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    private static void valueTag(EntityPlayer entityPlayer,int value,String string,String data) {
        IChatComponent iChatComponent=currentTag(indexTag(value) + string,EnumChatFormatting.DARK_PURPLE);
        iChatComponent.appendSibling(currentTag(data,EnumChatFormatting.YELLOW));
        entityPlayer.addChatMessage(iChatComponent);
    }

    private static void send(EntityPlayer entityPlayer,NBTTagCompound nbtTagCompound,int value) {
        if(value > 16) return;
        List<String> stringList=new ArrayList<String>((Set<String>)nbtTagCompound.func_150296_c());
        Collections.sort(stringList,String.CASE_INSENSITIVE_ORDER);
        for (String string : stringList) {
            NBTBase nbtBase=nbtTagCompound.getTag(string);
            if(nbtBase instanceof NBTTagCompound) {
                valueTag(entityPlayer,value,string,": {");
                send(entityPlayer,(NBTTagCompound)nbtBase,value + 1);
                valueTag(entityPlayer,value,"","}");
            } else if(nbtBase instanceof NBTTagList) {
                valueTag(entityPlayer,value,string,": [");
                sendList(entityPlayer,(NBTTagList)nbtBase,value + 1);
                valueTag(entityPlayer,value,"","]");
            } else if(nbtBase != null) {
                valueTag(entityPlayer,value,string,": " + nbtBase);
            }
        }
    }

    private static void sendList(EntityPlayer entityPlayer,NBTTagList nbtTagList,int value) {
        if(value > 16) return;
        NBTTagList tagList=(NBTTagList)nbtTagList.copy();
        int data=tagList.tagCount();
        for (int index=0;index < data;index++) {
            NBTBase nbtBase=tagList.removeTag(0);
            if(nbtBase instanceof NBTTagCompound) {
                valueTag(entityPlayer,value,"[" + index + "]"," {");
                send(entityPlayer,(NBTTagCompound)nbtBase,value + 1);
                valueTag(entityPlayer,value,"","}");
            } else if(nbtBase instanceof NBTTagList) {
                valueTag(entityPlayer,value,"[" + index + "]"," [");
                sendList(entityPlayer,(NBTTagList)nbtBase,value + 1);
                valueTag(entityPlayer,value,"","]");
            } else if(nbtBase != null) {
                valueTag(entityPlayer,value,"[" + index + "]"," " + nbtBase);
            }
        }
    }
}
