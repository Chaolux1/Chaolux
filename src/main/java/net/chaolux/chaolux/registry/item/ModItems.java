package net.chaolux.chaolux.registry.item;

import net.chaolux.chaolux.common.item.EyeItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS;
    public static final RegistryObject<Item> EYE;

    public static RegistryObject<Item> registerWithTab(String name, Supplier<Item> supplier) {
        return ITEMS.register(name, supplier);
    }

    public static Item.Properties basicItem() {
        return new Item.Properties();
    }

    static {
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "chaolux");
        EYE = registerWithTab("eye", () -> new EyeItem(basicItem().stacksTo(1)));
    }
}
