package net.chaolux.chaolux.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class CommandHistory {
    private static final int MAX=64;
    private static final File HISTORY=new File(Minecraft.getMinecraft().mcDataDir,"command_history.txt");
    private static final List<String> COMMAND=new ArrayList<String>();
    private static int value;
    public static void load() {
        COMMAND.clear();
        if(HISTORY.isFile()) {
            try {
                for (String string : Files.readAllLines(HISTORY.toPath(), StandardCharsets.UTF_8)) {
                    if(isCommand(string)) COMMAND.add(string.trim());
                }
            } catch (IOException ioException) {

            }
        }
        size();
        GuiNewChat guiNewChat=Minecraft.getMinecraft().ingameGUI.getChatGUI();
        List<String> stringList=guiNewChat.getSentMessages();
        for(int index=stringList.size() - 1;index >= 0;index--) {
            if(isCommand(stringList.get(index)))stringList.remove(index);
        }
        for(String string : COMMAND) {
            guiNewChat.addToSentMessages(string);
        }
        value=stringList.size();
    }

    public static void recordHistory() {
        Minecraft minecraft=Minecraft.getMinecraft();
        if(minecraft.ingameGUI == null) return;
        List<String> stringList=minecraft.ingameGUI.getChatGUI().getSentMessages();
        if(stringList.size() < value) value=stringList.size();
        if(stringList.size() == value) return;
        for(int index=value;index < stringList.size();index++) {
            String string=stringList.get(index);
            if(!isCommand(string)) continue;
            string=string.trim();
            if(COMMAND.isEmpty() || !string.equals(COMMAND.get(COMMAND.size() - 1))) COMMAND.add(string);
        }
        value=stringList.size();
        size();
        try {
            Files.write(HISTORY.toPath(),COMMAND,StandardCharsets.UTF_8);
        } catch (IOException ioException) {

        }
    }

    private static boolean isCommand(String string) {
        return string != null && string.trim().startsWith("/") && string.trim().length() > 1;
    }

    private static void size() {
        while (COMMAND.size() > MAX) {
            COMMAND.remove(0);
        }
    }

    public static void save() {
        value=0;
        COMMAND.clear();
    }
}
