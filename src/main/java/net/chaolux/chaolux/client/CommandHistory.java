package net.chaolux.chaolux.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class CommandHistory {
    private static final int MAX=64;
    private static final Path HISTORY= FMLPaths.GAMEDIR.get().resolve("command_history.txt");
    private static final List<String> COMMAND=new ArrayList<>();
    public static void loadChat() {
        load();
        Minecraft minecraft=Minecraft.getInstance();
        if(minecraft.gui == null) return;
        List<String> stringList=minecraft.gui.getChat().getRecentChat();
        stringList.removeIf(CommandHistory::isCommand);
        for (String string : COMMAND) {
            minecraft.gui.getChat().addRecentChat(string);
        }
    }

    public static void recordHistory() {
        Minecraft minecraft=Minecraft.getInstance();
        if(minecraft.gui == null) return;
        List<String> stringList=minecraft.gui.getChat().getRecentChat();
        if(stringList.isEmpty()) return;
        record(stringList.get(stringList.size() - 1));
    }

    private static void record(String string) {
        if(!isCommand(string)) return;
        String command=string.strip();
        load();
        if(!COMMAND.isEmpty() && COMMAND.get(COMMAND.size() - 1).equals(command)) return;
        COMMAND.add(command);
        size();
        save();
    }

    private static void load() {
        COMMAND.clear();
        if(!Files.exists(HISTORY)) return;
        try {
            for(String string : Files.readAllLines(HISTORY, StandardCharsets.UTF_8)) {
                String command=string.strip();
                if(isCommand(command)) COMMAND.add(command);
            }
            size();
        } catch (IOException exception) {

        }
    }

    private static void save() {
        try {
            Files.write(HISTORY,COMMAND,StandardCharsets.UTF_8, StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE);
        } catch (IOException exception) {

        }
    }

    private static void size() {
        while (COMMAND.size() > MAX) {
            COMMAND.remove(0);
        }
    }

    private static boolean isCommand(String string) {
        return string != null && string.startsWith("/") && string.length() > 1;
    }
}
