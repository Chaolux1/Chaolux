package net.chaolux.chaolux.common.event;

import net.chaolux.chaolux.registry.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = "chaolux", bus = Bus.FORGE)
public class EyeEvent {
    private static final int CHAOS=0x9A00CC;
    private static final int LUX=0xFFDF2F;
    private static final int SEPARATOR=0xA0A0A0;
    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific entityInteractSpecific) {
        if(!entityInteractSpecific.getItemStack().is(ModItems.EYE.get())) return;
        entityInteractSpecific.setCanceled(true);
        entityInteractSpecific.setCancellationResult(InteractionResult.SUCCESS);
        if(!entityInteractSpecific.getLevel().isClientSide && entityInteractSpecific.getEntity() instanceof ServerPlayer serverPlayer) copyTag(serverPlayer,"Entity " + entityInteractSpecific.getTarget().getName().getString(),new EntityDataAccessor(entityInteractSpecific.getTarget()).getData());
    }

    public static void getNoBlockEntity(ServerPlayer serverPlayer, BlockPos blockPos) {
        serverPlayer.sendSystemMessage(Component.literal("[Eye] ").withStyle(style -> style.withColor(CHAOS).withBold(true)).append(Component.literal("No BlockEntity " + blockPos.toShortString()).withStyle(ChatFormatting.GRAY)));
    }

    public static void copyTag(ServerPlayer serverPlayer, String string, CompoundTag compoundTag) {
        String copy=compoundTag.toString();
        Component component=Component.literal("[COPY]").withStyle(style -> style.withColor(LUX).withBold(true).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,copy)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,Component.literal("Copy full NBT"))));
        serverPlayer.sendSystemMessage(Component.literal("[EYE] ").withStyle(style -> style.withColor(CHAOS).withBold(true)).append(Component.literal(string).withStyle(ChatFormatting.WHITE)).append(component));
        send(serverPlayer,compoundTag,0);
    }

    private static void send(ServerPlayer serverPlayer,CompoundTag compoundTag,int value) {
        List<String> stringList=new ArrayList<>(compoundTag.getAllKeys());
        stringList.sort(String::compareToIgnoreCase);
        for(String string : stringList) {
            Tag tag=compoundTag.get(string);
            if(tag == null) continue;
            if(tag instanceof CompoundTag currentCompoundTag) {
                serverPlayer.sendSystemMessage(space(value).append(currentTag(string)).append(separation(": {")));
                send(serverPlayer,currentCompoundTag,value + 1);
                serverPlayer.sendSystemMessage(space(value).append(separation("}")));
                continue;
            }
            if(tag instanceof ListTag listTag && hasTag(listTag)) {
                serverPlayer.sendSystemMessage(space(value).append(currentTag(string)).append(separation(": [")));
                sendList(serverPlayer,listTag,value + 1);
                serverPlayer.sendSystemMessage(space(value).append(separation("]")));
                continue;
            }
            serverPlayer.sendSystemMessage(space(value).append(currentTag(string)).append(separation(": ")).append(valueTag(tag.toString())));
        }
    }

    private static void sendList(ServerPlayer serverPlayer,ListTag listTag,int value) {
        for (int index=0;index < listTag.size();index++) {
            Tag tag=listTag.get(index);
            if(tag instanceof CompoundTag compoundTag) {
                serverPlayer.sendSystemMessage(space(value).append(indexTag(index)).append(separation(" {")));
                send(serverPlayer,compoundTag,value + 1);
                serverPlayer.sendSystemMessage(space(value).append(separation("}")));
                continue;
            }
            if(tag instanceof ListTag tags && hasTag(tags)) {
                serverPlayer.sendSystemMessage(space(value).append(indexTag(index)).append(separation(" [")));
                sendList(serverPlayer,tags,value + 1);
                serverPlayer.sendSystemMessage(space(value).append(separation("]")));
                continue;
            }
            serverPlayer.sendSystemMessage(space(value).append(indexTag(index)).append(separation(" ")).append(valueTag(tag.toString())));
        }
    }

    private static boolean hasTag(ListTag listTag) {
        for (int index=0;index < listTag.size();index++) {
            Tag tag=listTag.get(index);
            if(tag instanceof CompoundTag || tag instanceof ListTag) {
                return true;
            }
        }
        return false;
    }

    private static MutableComponent space(int value) {
        return Component.literal(" ".repeat(Math.max(0,value)));
    }

    private static MutableComponent currentTag(String string) {
        return Component.literal(string).withStyle(style -> style.withColor(CHAOS));
    }

    private static MutableComponent indexTag(int index) {
        return Component.literal("[" + index + "]").withStyle(style -> style.withColor(CHAOS));
    }

    private static MutableComponent valueTag(String string) {
        return Component.literal(string).withStyle(style -> style.withColor(LUX));
    }

    private static MutableComponent separation(String string) {
        return Component.literal(string).withStyle(style -> style.withColor(SEPARATOR));
    }
}
