package zero.mods.fabric.tp4mods.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import zero.mods.fabric.tp4mods.Tp4Mods;

import java.util.function.Function;

public class ModItems {

    public static Item registerItem(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Tp4Mods.MOD_ID, name));
        return Items.register(registryKey, factory, settings);
    }

    private static void customGroup(FabricItemGroupEntries entries, Item customItem) {
        entries.add(customItem);
    }

    public static void registerModItems(Item customItem) {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> customGroup(entries, customItem));
    }
}
