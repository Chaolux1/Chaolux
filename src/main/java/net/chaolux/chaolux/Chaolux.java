package net.chaolux.chaolux;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.chaolux.chaolux.common.CommonProxy;
import net.chaolux.chaolux.common.event.EyeEvent;
import net.chaolux.chaolux.common.item.EyeItem;
import net.chaolux.chaolux.registry.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = "chaolux", name = "Chaolux", version = Tags.VERSION)
public class Chaolux {
    public static final String MOD_ID="chaolux";
    public static final String NAME="Chaolux";
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public static EyeItem eyeItem;

    @SidedProxy(clientSide = "net.chaolux.chaolux.client.ClientProxy",serverSide = "net.chaolux.chaolux.common.CommonProxy")
    public static CommonProxy commonProxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent fmlPreInitializationEvent) {
        eyeItem=new EyeItem();
        eyeItem.setUnlocalizedName("chaolux.eye");
        eyeItem.setTextureName("chaolux:eye");
        eyeItem.setMaxStackSize(1);
        eyeItem.setCreativeTab(CreativeTabs.tabTools);
        GameRegistry.registerItem(eyeItem,"eye");
        ModNetwork.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent ev) {
        MinecraftForge.EVENT_BUS.register(new EyeEvent());
        commonProxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent fmlPostInitializationEvent) {

    }
}
