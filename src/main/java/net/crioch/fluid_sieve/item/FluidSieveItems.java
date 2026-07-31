package net.crioch.fluid_sieve.item;

import net.crioch.fluid_sieve.FluidSieveMod;
import net.crioch.fluid_sieve.block.FluidSieveBlocks;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

public class FluidSieveItems {
    public static Item STRING_SIEVE;
    public static Item DENSE_SIEVE;

    public static Item STRING_MESH;
    public static Item DENSE_MESH;

    public static void register() {
        STRING_SIEVE = register("string_sieve", new BlockItem(FluidSieveBlocks.STRING_SIEVE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(FluidSieveMod.MOD_ID, "string_sieve")))));
        DENSE_SIEVE = register("dense_sieve", new BlockItem(FluidSieveBlocks.DENSE_SIEVE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(FluidSieveMod.MOD_ID, "dense_sieve")))));

        STRING_MESH = register("string_mesh", new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(FluidSieveMod.MOD_ID, "string_mesh")))));
        DENSE_MESH = register("dense_mesh", new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(FluidSieveMod.MOD_ID, "dense_mesh")))));

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.accept(STRING_SIEVE);
            entries.accept(DENSE_SIEVE);
        });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            entries.accept(STRING_MESH);
            entries.accept(DENSE_MESH);
        });
    }

    private static Item register(String id, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(FluidSieveMod.MOD_ID, id), item);
    }
}
