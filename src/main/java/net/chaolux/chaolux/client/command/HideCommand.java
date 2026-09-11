package net.chaolux.chaolux.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class HideCommand {
    private static final Set<String> HIDDEN=new HashSet<>();
    public static void register(RegisterClientCommandsEvent registerClientCommandsEvent) {
        registerClientCommandsEvent.getDispatcher().register(Commands.literal("hide").then(Commands.argument("player", StringArgumentType.word()).suggests((command,builder) -> {
            Minecraft minecraft=Minecraft.getInstance();
            if(minecraft.level == null) return builder.buildFuture();
            return SharedSuggestionProvider.suggest(minecraft.level.players().stream().map(clientPlayer -> clientPlayer.getGameProfile().getName()),builder);
        }).executes(commandContext -> toggle(StringArgumentType.getString(commandContext,"player")))));
    }

    private static int toggle(String string) {
        Minecraft minecraft=Minecraft.getInstance();
        if(minecraft.player == null) return 0;
        String normal=string.toLowerCase(Locale.ROOT);
        if(HIDDEN.remove(normal)) {
            minecraft.player.displayClientMessage(Component.literal("Show: ").withStyle(ChatFormatting.DARK_PURPLE).append(Component.literal(string).withStyle(ChatFormatting.YELLOW)),false);
            return 1;
        }
        HIDDEN.add(normal);
        minecraft.player.displayClientMessage(Component.literal("Hide: ").withStyle(ChatFormatting.DARK_PURPLE).append(Component.literal(string).withStyle(ChatFormatting.YELLOW)),false);
        return 1;
    }

    public static void renderNameTag(RenderNameTagEvent renderNameTagEvent) {
        if(!(renderNameTagEvent.getEntity() instanceof Player player)) return;
        String string=player.getGameProfile().getName().toLowerCase(Locale.ROOT);
        if(HIDDEN.contains(string)) renderNameTagEvent.setResult(Event.Result.DENY);
    }
}
