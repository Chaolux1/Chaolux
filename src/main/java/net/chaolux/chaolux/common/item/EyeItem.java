package net.chaolux.chaolux.common.item;

import net.chaolux.chaolux.common.event.EyeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class EyeItem extends Item {
    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world,int x,int y,int z,int value,float mouseX,float mouseY,float mouseZ) {
        if(!world.isRemote) {
            TileEntity tileEntity=world.getTileEntity(x,y,z);
            if(tileEntity == null) {
                EyeEvent.getNoBlockEntity(entityPlayer,x,y,z);
            } else {
                NBTTagCompound nbtTagCompound=new NBTTagCompound();
                tileEntity.writeToNBT(nbtTagCompound);
                EyeEvent.copyTag(entityPlayer,"BlockEntity " + x + " " + y + " " + z,nbtTagCompound);
            }
        }
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack,World world,EntityPlayer entityPlayer) {
        if(!world.isRemote) {
            NBTTagCompound nbtTagCompound=new NBTTagCompound();
            entityPlayer.writeToNBT(nbtTagCompound);
            EyeEvent.copyTag(entityPlayer,"Player " + entityPlayer.getCommandSenderName(),nbtTagCompound);
        }
        return itemStack;
    }
}
