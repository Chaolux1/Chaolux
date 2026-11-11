package net.chaolux.chaolux.client.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class HideCommand extends CommandBase {
    private static final Set<String> HIDDEN=new HashSet<String>();

    @Override
    public String getCommandName() {
        return "hide";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "/hide <player>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender iCommandSender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender iCommandSender,String[] strings) {
        if(strings.length != 1) {
            iCommandSender.addChatMessage(new ChatComponentText(getCommandUsage(iCommandSender)));
            return;
        }
        String string=strings[0].toLowerCase(Locale.ROOT);
        boolean hidden=HIDDEN.remove(string);
        if(!hidden) HIDDEN.add(string);
        iCommandSender.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_PURPLE + (hidden ? "Show: " : "Hide: ") + EnumChatFormatting.YELLOW + strings[0]));
    }

    public static boolean isHidden(String string) {
        return HIDDEN.contains(string.toLowerCase(Locale.ROOT));
    }

    public static void clear() {
        HIDDEN.clear();
    }
}
