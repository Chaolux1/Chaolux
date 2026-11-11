package net.chaolux.chaolux.client.command;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.LinkedHashMap;
import java.util.Map;

public class CopyCommand extends CommandBase {
    private static final Map<Integer,String> MAP=new LinkedHashMap<Integer,String>();

    @Override
    public String getCommandName() {
        return "chaoluxcopy";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "/chaoluxcopy <id>";
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
        if(strings.length != 1) return;
        try {
            String string;
            synchronized (CopyCommand.class) {
                string=MAP.get(Integer.parseInt(strings[0]));
            }
            if(string != null) {
                GuiScreen.setClipboardString(string);
                iCommandSender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "[EYE] NBT copy"));
            }
        } catch (NumberFormatException numberFormatException) {

        }
    }

    public static synchronized void sync(int value,String string) {
        MAP.put(value,string);
        while (MAP.size() > 16) {
            MAP.remove(MAP.keySet().iterator().next());
        }
    }

    public static synchronized void clear() {
        MAP.clear();
    }

}
