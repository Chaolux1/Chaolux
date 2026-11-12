package net.chaolux.chaolux.registry.item;

import net.chaolux.chaolux.common.item.EyeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;


import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS;
    public static final Supplier<Item> EYE;

    public static Supplier<Item> registerWithTab(String name, Supplier<Item> supplier) {
        Supplier<Item> block = ITEMS.register(name, supplier);
        return block;
    }

    public static Item.Properties basicItem() {
        return new Item.Properties();
    }

    static {
        ITEMS = DeferredRegister.create(Registries.ITEM, "chaolux");
        EYE = registerWithTab("eye", () -> new EyeItem(basicItem().stacksTo(1)));
    }
}
