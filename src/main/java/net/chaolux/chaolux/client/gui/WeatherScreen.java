package net.chaolux.chaolux.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.chaolux.chaolux.common.network.Action;
import net.chaolux.chaolux.common.network.WeatherPacket;
import net.chaolux.chaolux.registry.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class WeatherScreen extends Screen {
    static final ResourceLocation GAMEMODE_SWITCHER_LOCATION = new ResourceLocation("textures/gui/container/gamemode_switcher.png");
    private static final int SPRITE_SHEET_WIDTH = 128;
    private static final int SPRITE_SHEET_HEIGHT = 128;
    private static final int SLOT_AREA = 26;
    private static final int SLOT_AREA_PADDED = 31;
    private static final int ALL_SLOTS_WIDTH = WeatherIcon.values().length * 31 - 5;
    private static final int WIDTH=ALL_SLOTS_WIDTH + 6;
    private static final int HEIGHT=75;
    private static final Component SELECT_KEY;
    private static WeatherIcon lastHovered=WeatherIcon.DAY;
    private final WeatherIcon previousHovered = lastHovered;
    private WeatherIcon currentlyHovered;
    private int firstMouseX;
    private int firstMouseY;
    private boolean setFirstMousePos;
    private final List<WeatherSlot> slots = Lists.newArrayList();
    private EditBox editBox;

    public WeatherScreen() {
        super(GameNarrator.NO_TITLE);
        this.currentlyHovered = this.previousHovered;
    }

    static {
        SELECT_KEY = Component.translatable("debug.gamemodes.select_next", new Object[]{Component.translatable("screen.chaolux.weather_f6").withStyle(ChatFormatting.AQUA)});
    }

    @Override
    protected void init() {
        super.init();
        this.slots.clear();
        this.currentlyHovered=this.previousHovered;
        WeatherIcon[] weatherIcons=WeatherIcon.WEATHER;
        for(int index=0;index < weatherIcons.length;index++) {
            WeatherIcon weatherIcon=weatherIcons[index];
            this.slots.add(new WeatherSlot(weatherIcon,this.width / 2 - ALL_SLOTS_WIDTH / 2 + index * SLOT_AREA_PADDED,this.height / 2 - SLOT_AREA_PADDED));
        }
        this.editBox=new EditBox(this.font,this.width / 2 + 8,this.height / 2 - SLOT_AREA_PADDED - 22,24,11,Component.translatable("screen.chaolux.custom_time"));
        this.editBox.setMaxLength(2);
        this.editBox.setValue("");
        this.editBox.setHint(Component.literal("0-23").withStyle(ChatFormatting.DARK_GRAY));
        this.editBox.setFilter(WeatherScreen::isValid);
        this.addRenderableWidget(this.editBox);
        updateTime();
    }

    @Override
    public void render(GuiGraphics guiGraphics,int x,int y,float tick) {
        if(close()) return;
        guiGraphics.pose().pushPose();
        RenderSystem.enableBlend();
        float screenX=(float) WIDTH / 125.0f;
        int width=this.width / 2 - WIDTH / 2;
        int height=this.height / 2 - SLOT_AREA_PADDED - 27;
        int drawX=this.currentlyHovered == WeatherIcon.CUSTOM_TIME ? this.width / 2 - 25 : this.width / 2;
        guiGraphics.pose().translate(width,height,0.0f);
        guiGraphics.pose().scale(screenX,1.0f,1.0f);
        guiGraphics.blit(GAMEMODE_SWITCHER_LOCATION,0,0,0.0f,0.0f,125,HEIGHT,SPRITE_SHEET_WIDTH,SPRITE_SHEET_HEIGHT);
        guiGraphics.pose().popPose();
        super.render(guiGraphics,x,y,tick);
        guiGraphics.drawCenteredString(this.font,getCurrent(),drawX,this.height / 2 - SLOT_AREA_PADDED - 20,-1);
        guiGraphics.drawCenteredString(this.font,SELECT_KEY,this.width / 2,this.height / 2 + 5,0xFFFFFF);
        if(!this.setFirstMousePos) {
            this.firstMouseX=x;
            this.firstMouseY=y;
            this.setFirstMousePos=true;
        }
        boolean NoMove=this.firstMouseX == x && this.firstMouseY == y;
        for(WeatherSlot weatherSlot : this.slots) {
            weatherSlot.setCurrent(this.currentlyHovered == weatherSlot.weatherIcon);
            weatherSlot.render(guiGraphics,x,y,tick);
            if (!NoMove && weatherSlot.isHoveredOrFocused()) {
                if(this.currentlyHovered != weatherSlot.weatherIcon) {
                    this.currentlyHovered=weatherSlot.weatherIcon;
                    updateTime();
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int key,int scanValue,int value) {
        if(key == GLFW.GLFW_KEY_F6) {
            this.setFirstMousePos=false;
            this.currentlyHovered=this.currentlyHovered.getNext();
            updateTime();
            return true;
        }
        if(key == GLFW.GLFW_KEY_RIGHT) {
            this.setFirstMousePos=false;
            this.currentlyHovered=this.currentlyHovered.getNext();
            updateTime();
            return true;
        }
        if(key == GLFW.GLFW_KEY_LEFT) {
            this.setFirstMousePos=false;
            this.currentlyHovered=this.currentlyHovered.getPrevios();
            updateTime();
            return true;
        }
        return super.keyPressed(key,scanValue,value);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Component getCurrent() {
        return this.currentlyHovered.getComponent();
    }

    private boolean close() {
        if(!InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(),GLFW.GLFW_KEY_F5)) {
            switchWeather();
            this.minecraft.setScreen(null);
            return true;
        }
        return false;
    }

    private void switchWeather() {
        if(this.minecraft == null || this.minecraft.player == null) return;
        int time=12;
        if(this.currentlyHovered == WeatherIcon.CUSTOM_TIME) {
            try {
                time=Integer.parseInt(this.editBox.getValue());
            } catch (NumberFormatException exception) {
                time=12;
            }
            time=Math.max(0,Math.min(23,time));
        }
        ModNetwork.INSTANCE.sendToServer(new WeatherPacket(this.currentlyHovered.action,time));
        lastHovered=this.currentlyHovered;
    }

    private static boolean isValid(String string) {
        if(string.isEmpty()) return true;
        if(!string.chars().allMatch(Character::isDigit)) return false;
        try {
            int time=Integer.parseInt(string);
            return time >= 0 && time <= 23;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private void updateTime() {
        if(this.editBox == null) return;
        boolean customTime=this.currentlyHovered == WeatherIcon.CUSTOM_TIME;
        this.editBox.setVisible(customTime);
        this.editBox.setEditable(customTime);
        if(customTime) {
            this.setFocused(this.editBox);
            this.editBox.setFocused(true);
        } else {
            this.editBox.setFocused(false);
            if(this.getFocused() == this.editBox) this.setFocused(null);
        }
    }

    private enum WeatherIcon {
        DAY(Component.literal("Day"),new ItemStack(Items.SUNFLOWER),Action.DAY),NIGHT(Component.literal("Night"),new ItemStack(Items.WITHER_ROSE),Action.NIGHT),RAIN(Component.literal("Rain"),new ItemStack(Items.WATER_BUCKET),Action.RAIN),THUNDER(Component.literal("Thunder"),new ItemStack(Items.TRIDENT),Action.THUNDER),CLEAR(Component.literal("Clear"),new ItemStack(Items.GLASS),Action.CLEAR),CUSTOM_TIME(Component.literal("Custom Time"),new ItemStack(Items.CLOCK),Action.CUSTOM_TIME);
        private static final WeatherIcon[] WEATHER=values();
        private static final int ICON=5;
        private final Component component;
        private final ItemStack itemStack;
        private final Action action;
        WeatherIcon(Component component,ItemStack itemStack,Action action) {
            this.component=component;
            this.itemStack=itemStack;
            this.action=action;
        }
        void drawIcon(GuiGraphics guiGraphics,int x,int y) {
            guiGraphics.renderItem(this.itemStack,x,y);
        }
        Component getComponent() {
            return this.component;
        }
        WeatherIcon getNext() {
            return switch (this) {
                case DAY -> NIGHT;
                case NIGHT -> RAIN;
                case RAIN -> THUNDER;
                case THUNDER -> CLEAR;
                case CLEAR -> CUSTOM_TIME;
                case CUSTOM_TIME -> DAY;
            };
        }
        WeatherIcon getPrevios() {
            return switch (this) {
                case DAY -> CUSTOM_TIME;
                case NIGHT -> DAY;
                case RAIN -> NIGHT;
                case THUNDER -> RAIN;
                case CLEAR -> THUNDER;
                case CUSTOM_TIME -> CLEAR;
            };
        }
    }

    private class WeatherSlot extends AbstractWidget {
        private final WeatherIcon weatherIcon;
        private boolean isCurrent;
        public WeatherSlot(WeatherIcon weatherIcon,int x,int y) {
            super(x,y,SLOT_AREA,SLOT_AREA,weatherIcon.getComponent());
            this.weatherIcon=weatherIcon;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics,int x,int y,float tick) {
            drawSlot(guiGraphics);
            this.weatherIcon.drawIcon(guiGraphics,this.getX() + WeatherIcon.ICON,this.getY() + WeatherIcon.ICON);
            if(this.isCurrent) drawCurrent(guiGraphics);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        @Override
        public boolean isHoveredOrFocused() {
            return super.isHoveredOrFocused() || this.isCurrent;
        }

        public void setCurrent(boolean current) {
            this.isCurrent=current;
        }

        private void drawSlot(GuiGraphics guiGraphics) {
            guiGraphics.blit(GAMEMODE_SWITCHER_LOCATION,this.getX(),this.getY(),0.0f,75.0f,26,26,SPRITE_SHEET_WIDTH,SPRITE_SHEET_HEIGHT);
        }

        private void drawCurrent(GuiGraphics guiGraphics) {
            guiGraphics.blit(GAMEMODE_SWITCHER_LOCATION,this.getX(),this.getY(),26.0f,75.0f,26,26,SPRITE_SHEET_WIDTH,SPRITE_SHEET_HEIGHT);
        }
    }

}
