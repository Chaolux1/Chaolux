package net.chaolux.chaolux.registry.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.chaolux.chaolux.client.command.CopyCommand;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class ModNetwork {
    public static final SimpleNetworkWrapper INSTANCE= NetworkRegistry.INSTANCE.newSimpleChannel("chaolux");
    private static final AtomicInteger ATOMIC_INTEGER=new AtomicInteger();
    private static final ConcurrentLinkedDeque<Queue> QUEUES=new ConcurrentLinkedDeque<Queue>();
    private static class Queue {
        final EntityPlayerMP entityPlayerMP;
        final int value;
        final int data;
        Queue(EntityPlayerMP entityPlayerMP,int value,int data) {
            this.entityPlayerMP=entityPlayerMP;
            this.value=value;
            this.data=data;
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent serverTickEvent) {
        if(serverTickEvent.phase != TickEvent.Phase.END) return;
        Queue queue;
        while ((queue=QUEUES.poll()) != null) apply(queue);
    }

    public static void init() {
        INSTANCE.registerMessage(ActionHandler.class,ActionPacket.class,0, Side.SERVER);
        INSTANCE.registerMessage(CopyHandler.class,CopyPacket.class,1, Side.CLIENT);
        FMLCommonHandler.instance().bus().register(new ModNetwork());
    }

    public static int copy(EntityPlayerMP entityPlayerMP,String string) {
        byte[] bytes=string.getBytes(StandardCharsets.UTF_8);
        if(bytes.length > 24000) {
            entityPlayerMP.addChatMessage(new ChatComponentText("NBT too large"));
            return -1;
        }
        int queue=ATOMIC_INTEGER.incrementAndGet();
        INSTANCE.sendTo(new CopyPacket(queue,string),entityPlayerMP);
        return queue;
    }

    public static class ActionPacket implements IMessage {
        public int value;
        public int data;
        public ActionPacket() {

        }
        public ActionPacket(int value,int data) {
            this.value=value;
            this.data=data;
        }

        @Override
        public void fromBytes(ByteBuf byteBuf) {
            value=byteBuf.readUnsignedByte();
            data=byteBuf.readInt();
        }

        @Override
        public void toBytes(ByteBuf byteBuf) {
            byteBuf.writeByte(value);
            byteBuf.writeInt(data);
        }
    }

    public static class ActionHandler implements IMessageHandler<ActionPacket,IMessage> {
        @Override
        public IMessage onMessage(ActionPacket actionPacket, MessageContext messageContext) {
            QUEUES.add(new Queue(messageContext.getServerHandler().playerEntity,actionPacket.value,actionPacket.data));
            return null;
        }
    }

    private static void apply(Queue queue) {
        EntityPlayerMP entityPlayerMP=queue.entityPlayerMP;
        if(entityPlayerMP == null || entityPlayerMP.playerNetServerHandler == null || entityPlayerMP.isDead) return;
        if(!entityPlayerMP.canCommandSenderUseCommand(2,"gamemode")) {
            ChatComponentTranslation chatComponentTranslation=new ChatComponentTranslation("command.generic.permission");
            chatComponentTranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            entityPlayerMP.addChatMessage(chatComponentTranslation);
            return;
        }
        if(queue.value == 6) {
            if(queue.data < 0 || queue.data > 2) return;
            WorldSettings.GameType gameType=WorldSettings.GameType.getByID(queue.data);
            entityPlayerMP.setGameType(gameType);
            entityPlayerMP.fallDistance=0;
            entityPlayerMP.sendPlayerAbilities();
            entityPlayerMP.addChatMessage(new ChatComponentTranslation("gameMode.changed",gameType.getName()));
            return;
        }
        WorldServer worldServer=(WorldServer)entityPlayerMP.worldObj;
        switch (queue.value) {
            case 0: setTime(entityPlayerMP,1000);
            break;
            case 1: setTime(entityPlayerMP,13000);
            break;
            case 2: worldServer.getWorldInfo().setRaining(true);
            worldServer.getWorldInfo().setThundering(false);
            worldServer.getWorldInfo().setRainTime(12000);
            break;
            case 3: worldServer.getWorldInfo().setRaining(true);
            worldServer.getWorldInfo().setThundering(true);
            worldServer.getWorldInfo().setRainTime(12000);
            worldServer.getWorldInfo().setThunderTime(12000);
            break;
            case 4: worldServer.getWorldInfo().setRaining(false);
            worldServer.getWorldInfo().setThundering(false);
            worldServer.getWorldInfo().setRainTime(0);
            worldServer.getWorldInfo().setThunderTime(0);
            break;
            case 5: if(queue.data >= 0 && queue.data < 24) setTime(entityPlayerMP,((queue.data - 6 + 24) % 24) * 1000);
            break;
            default: break;
        }
    }

    private static void setTime(EntityPlayerMP entityPlayerMP,long value) {
        for (WorldServer worldServer : entityPlayerMP.mcServer.worldServers) {
            long data=worldServer.getWorldTime();
            long metadata=data - ((data % 24000 + 24000) % 24000);
            worldServer.setWorldTime(metadata + value);
        }
    }

    public static class CopyPacket implements IMessage {
        public int value;
        public String string;
        public CopyPacket() {

        }
        public CopyPacket(int value,String string) {
            this.value=value;
            this.string=string;
        }

        @Override
        public void fromBytes(ByteBuf byteBuf) {
            value=byteBuf.readInt();
            int data=byteBuf.readUnsignedShort();
            if(data > 24000 || data > byteBuf.readableBytes()) {
                string="";
                return;
            }
            byte[] bytes=new byte[data];
            byteBuf.readBytes(bytes);
            string=new String(bytes,StandardCharsets.UTF_8);
        }

        @Override
        public void toBytes(ByteBuf byteBuf) {
            byte[] bytes=string.getBytes(StandardCharsets.UTF_8);
            byteBuf.writeInt(value);
            byteBuf.writeShort(bytes.length);
            byteBuf.writeBytes(bytes);
        }
    }

    public static class CopyHandler implements IMessageHandler<CopyPacket,IMessage> {
        @Override
        public IMessage onMessage(CopyPacket copyPacket,MessageContext messageContext) {
            if(copyPacket.string != null && !copyPacket.string.isEmpty()) CopyCommand.sync(copyPacket.value,copyPacket.string);
            return null;
        }
    }
}
