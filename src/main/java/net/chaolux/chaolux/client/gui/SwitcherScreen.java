package net.chaolux.chaolux.client.gui;

import com.google.common.collect.Lists;
import net.chaolux.chaolux.registry.network.ModNetwork;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldSettings;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.util.List;

public class SwitcherScreen extends GuiScreen {
    private static final ResourceLocation GAMEMODE_SWITCHER_LOCATION=new ResourceLocation("chaolux","textures/gui/gamemode_switcher.png");
    private static final int SPRITE_SHEET_WIDTH = 128;
    private static final int SPRITE_SHEET_HEIGHT = 128;
    private static final int SLOT_AREA = 26;
    private static final int SLOT_AREA_PADDED = 31;
    private static final int ALL_SLOTS_WIDTH = 6 * SLOT_AREA_PADDED - 5;
    private static final int ALL_SLOTS_HEIGHT = 75;
    private static final int WIDTH=125;
    private static final int HEIGHT=75;
    private static int lastHovered;
    private static final int previousHovered = -1;
    private static int previousGameMode = -1;
    private int firstMouseX;
    private int firstMouseY;
    private boolean setFirstMousePos;
    private static final String[] WEATHER= {"Day","Night","Rain","Thunder","Clear","Custom Time"};
    private static final String[] MODES={"Creative","Survival","Adventure"};
    private final Type type;
    private final int gameMode;
    private final RenderItem renderItem=new RenderItem();
    private int currentlyHovered;
    private boolean value;
    private GuiTextField guiTextField;
    public SwitcherScreen(Type type) {
        this.type=type;
        this.gameMode=type == Type.GAMEMODE ? getGameMode() : -1;
        if(type == Type.WEATHER) {
            this.currentlyHovered=lastHovered;
        } else {
            int value=previousGameMode;
            if(value == previousHovered || value == gameMode) value=gameMode == 1 ? 0 : 1;
            this.currentlyHovered=modeSlot(value);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        this.setFirstMousePos=false;
        if(this.type == Type.WEATHER) {
            this.guiTextField=new GuiTextField(this.fontRendererObj,this.width / 2 + 8,this.height / 2 - SLOT_AREA_PADDED - 22,24,11);
            this.guiTextField.setMaxStringLength(2);
            this.guiTextField.setText("");
            updateTime();
        }
    }

    @Override
    public void drawScreen(int x,int y,float value) {
        if(close()) return;
        int backgroundWidth=getBackgroundWidth();
        int backgroundHeight=getBackgroundHeight();
        backgroundTexture();
        drawTexture(backgroundWidth,backgroundHeight,getWidth(),ALL_SLOTS_HEIGHT,0,0,WIDTH,HEIGHT);
        for(int index=0;index < getCurrentlyHovered();index++) {
            drawSlot(index);
        }
        currentTexture();
        if(!this.setFirstMousePos) {
            this.firstMouseX=x;
            this.firstMouseY=y;
            this.setFirstMousePos=true;
        }
        boolean noMove=this.firstMouseX == x && this.firstMouseY == y;
        if(!noMove) {
            for(int index=0;index < getCurrentlyHovered();index++) {
                if(x >= getSlotAreaWidth(index) && x < getSlotAreaWidth(index) + SLOT_AREA && y >= getSlotAreaHeight() && y < getSlotAreaHeight() + SLOT_AREA) {
                    if(this.currentlyHovered != index) {
                        this.currentlyHovered=index;
                        updateTime();
                    }
                    break;
                }
            }
        }
        backgroundTexture();
        drawCurrent(this.currentlyHovered);
        currentTexture();
        RenderHelper.enableGUIStandardItemLighting();
        for(int index=0;index < getCurrentlyHovered();index++) {
            drawIcon(index);
        }
        RenderHelper.disableStandardItemLighting();
        int mouseX=this.type == Type.WEATHER && this.currentlyHovered == 5 ? this.width / 2 - 25 : this.width / 2;
        drawCenteredString(this.fontRendererObj,getCurrent(),mouseX,this.height / 2 - SLOT_AREA_PADDED - 20,0xFFFFFF);
        GL11.glPushMatrix();
        GL11.glTranslatef(this.width / 2.0f,this.height / 2.0f + 5.0f,0.0f);
        float scale=this.type == Type.WEATHER ? 1.0f : 0.7f;
        GL11.glScalef(scale,scale,1.0f);
        drawCenteredString(this.fontRendererObj,this.type == Type.WEATHER ? "[F6] Next / Release F5" : "[F4] Next / Release F3",0,0,0xFFFFFF);
        GL11.glPopMatrix();
        if(this.guiTextField != null && this.currentlyHovered == 5) {
            this.guiTextField.drawTextBox();
            if(this.guiTextField.getText().isEmpty() && !this.guiTextField.isFocused()) drawString(this.fontRendererObj,"0-23",this.width / 2 + 12,this.height / 2 - SLOT_AREA_PADDED - 20,0xFF777777);
        }
        super.drawScreen(x,y,value);
    }

    @Override
    protected void keyTyped(char value,int data) {
        if(data == Keyboard.KEY_ESCAPE) {
            this.value=true;
            this.mc.displayGuiScreen(null);
            return;
        }
        if((this.type == Type.WEATHER && data == Keyboard.KEY_F6) || (this.type == Type.GAMEMODE && data == Keyboard.KEY_F4) || data == Keyboard.KEY_RIGHT) {
            this.setFirstMousePos=false;
            this.currentlyHovered=getNext();
            updateTime();
            return;
        }
        if(data == Keyboard.KEY_LEFT) {
            this.setFirstMousePos=false;
            this.currentlyHovered=getPrevios();
            updateTime();
            return;
        }
        if(this.guiTextField != null && this.currentlyHovered == 5) {
            String string=this.guiTextField.getText();
            if(this.guiTextField.textboxKeyTyped(value,data)) {
                if(!isValid(this.guiTextField.getText())) this.guiTextField.setText(string);
            }
        }
    }

    @Override
    protected void mouseClicked(int x,int y,int value) {
        super.mouseClicked(x,y,value);
        if(value == 0) {
            for (int index=0;index < getCurrentlyHovered();index++) {
                if(x >= getSlotAreaWidth(index) && x < getSlotAreaWidth(index) + SLOT_AREA && y >= getSlotAreaHeight() && y < getSlotAreaHeight() + SLOT_AREA) {
                    this.currentlyHovered=index;
                    updateTime();
                    break;
                }
            }
        }
        if(this.guiTextField != null && this.currentlyHovered == 5) this.guiTextField.mouseClicked(x,y,value);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if(this.guiTextField != null) this.guiTextField.updateCursorCounter();
        close();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public static void resetMode() {
        previousGameMode = -1;
    }

    public Type getType() {
        return this.type;
    }

    private static int getGameMode() {
        Minecraft minecraft=Minecraft.getMinecraft();
        if(minecraft.playerController == null) return 0;
        try {
            for(Field field : minecraft.playerController.getClass().getDeclaredFields()) {
                if(field.getType() != WorldSettings.GameType.class) continue;
                field.setAccessible(true);
                Object object=field.get(minecraft.playerController);
                if(object instanceof WorldSettings.GameType) {
                    int value=((WorldSettings.GameType)object).getID();
                    if(value >= 0 && value <= 2) return value;
                }
            }
        } catch (ReflectiveOperationException reflectiveOperationException) {

        }
        return minecraft.playerController.isInCreativeMode() ? 1 : 0;
    }

    private static int modeSlot(int value) {
        return value == 1 ? 0 : value == 0 ? 1 : 2;
    }

    private static int mode(int value) {
        return value == 0 ? 1 : value == 1 ? 0 : 2;
    }

    private int getCurrentlyHovered() {
        return this.type == Type.WEATHER ? WEATHER.length : MODES.length;
    }

    private int getAllSlotsWidth() {
        return this.type == Type.WEATHER ? ALL_SLOTS_WIDTH : MODES.length * SLOT_AREA_PADDED - 5;
    }

    private int getWidth() {
        return getAllSlotsWidth() + 6;
    }

    private int getSlotAreaWidth(int value) {
        return this.width / 2 - getAllSlotsWidth() / 2 + value * SLOT_AREA_PADDED;
    }

    private int getSlotAreaHeight() {
        return this.height / 2 - SLOT_AREA_PADDED;
    }

    private int getBackgroundWidth() {
        return this.width / 2 - getWidth() / 2;
    }

    private int getBackgroundHeight() {
        return this.height / 2 - SLOT_AREA_PADDED - 27;
    }

    private String getCurrent() {
        if(this.type == Type.WEATHER) return WEATHER[this.currentlyHovered];
        return MODES[this.currentlyHovered] + " Mode";
    }

    private int getNext() {
        return (this.currentlyHovered + 1) % getCurrentlyHovered();
    }

    private int getPrevios() {
        return (this.currentlyHovered + getCurrentlyHovered() - 1) % getCurrentlyHovered();
    }

    private boolean close() {
        if(this.value) return true;
        boolean value=Keyboard.isKeyDown(this.type == Type.WEATHER ? Keyboard.KEY_F5 : Keyboard.KEY_F3);
        if(!value) {
            this.value=true;
            if(this.type == Type.WEATHER) {
                switchWeather();
            } else {
                switchGameMode();
            }
            this.mc.displayGuiScreen(null);
            return true;
        }
        return false;
    }

    private void switchWeather() {
        if(this.mc == null || this.mc.thePlayer == null) return;
        int value=12;
        if(this.currentlyHovered == 5 && this.guiTextField != null) {
            try {
                value=Integer.parseInt(this.guiTextField.getText());
            } catch (NumberFormatException numberFormatException) {
                value=12;
            }
            value=Math.max(0,Math.min(23,value));
        }
        ModNetwork.INSTANCE.sendToServer(new ModNetwork.ActionPacket(this.currentlyHovered,value));
        lastHovered=this.currentlyHovered;
    }

    private void switchGameMode() {
        if(this.mc == null || this.mc.thePlayer == null) return;
        int value=mode(this.currentlyHovered);
        if(value == this.gameMode) return;
        ModNetwork.INSTANCE.sendToServer(new ModNetwork.ActionPacket(6,value));
        previousGameMode=this.gameMode;
    }

    private static boolean isValid(String string) {
        if(string.isEmpty()) return true;
        for (int index=0;index < string.length();index++) {
            if(string.charAt(index) < '0' || string.charAt(index) > '9') return false;
        }
        try {
            int value=Integer.parseInt(string);
            return value >= 0 && value <= 23;
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }

    private void updateTime() {
        if(this.guiTextField == null) return;
        boolean value=this.type == Type.WEATHER && this.currentlyHovered == 5;
        this.guiTextField.setFocused(value);
    }

    private void drawSlot(int value) {
        drawTexture(getSlotAreaWidth(value),getSlotAreaHeight(),SLOT_AREA,SLOT_AREA,0,75,SLOT_AREA,SLOT_AREA);
    }

    private void drawCurrent(int value) {
        drawTexture(getSlotAreaWidth(value),getSlotAreaHeight(),SLOT_AREA,SLOT_AREA,26,75,SLOT_AREA,SLOT_AREA);
    }

    private void drawIcon(int value) {
        ItemStack itemStack=getIcon(value);
        this.renderItem.renderItemAndEffectIntoGUI(this.fontRendererObj,this.mc.getTextureManager(),itemStack,getSlotAreaWidth(value) + 5,getSlotAreaHeight() + 5);
    }

    private ItemStack getIcon(int value) {
        if(this.type == Type.GAMEMODE) {
            switch (value) {
                case 0: return new ItemStack(Blocks.grass);
                case 1: return new ItemStack(Items.iron_sword);
                case 2: return new ItemStack(Items.map);
                default: return new ItemStack(Blocks.grass);
            }
        }
        switch (value) {
            case 0: return new ItemStack(Items.clock);
            case 1: return new ItemStack(Items.bed);
            case 2: return new ItemStack(Items.water_bucket);
            case 3: return new ItemStack(Items.blaze_rod);
            case 4: return new ItemStack(Blocks.glass);
            case 5: return new ItemStack(Items.clock);
            default: return new ItemStack(Items.clock);
        }
    }

    private void backgroundTexture() {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0f,1.0f,1.0f,1.0f);
        this.mc.getTextureManager().bindTexture(GAMEMODE_SWITCHER_LOCATION);
    }

    private void currentTexture() {
        GL11.glPopAttrib();
    }

    private void drawTexture(int x,int y,int width,int height,int mouseX,int mouseY,int textureWidth,int textureHeight) {
        double minU=(double)mouseX / SPRITE_SHEET_WIDTH;
        double maxU=(double)(mouseX + textureWidth) / SPRITE_SHEET_WIDTH;
        double minV=(double)mouseY / SPRITE_SHEET_HEIGHT;
        double maxV=(double)(mouseY + textureHeight) / SPRITE_SHEET_HEIGHT;
        Tessellator tessellator=Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x,y + height,this.zLevel,minU,maxV);
        tessellator.addVertexWithUV(x + width,y + height,this.zLevel,maxU,maxV);
        tessellator.addVertexWithUV(x + width,y,this.zLevel,maxU,minV);
        tessellator.addVertexWithUV(x ,y,this.zLevel,minU,minV);
        tessellator.draw();
    }
}
