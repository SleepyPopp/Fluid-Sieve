package net.crioch.fluid_sieve.block;

import net.crioch.fluid_sieve.FluidSieveMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public class FluidSieveBlocks {
    public static Block STRING_SIEVE;
    public static Block DENSE_SIEVE;

    public static void register() {
        STRING_SIEVE = register("string_sieve", new BaseSieve(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).pushReaction(PushReaction.DESTROY), Identifier.fromNamespaceAndPath(FluidSieveMod.MOD_ID, "string_sieve")));
        DENSE_SIEVE = register("dense_sieve", new BaseSieve(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).pushReaction(PushReaction.DESTROY), Identifier.fromNamespaceAndPath(FluidSieveMod.MOD_ID, "dense_sieve")));
    }

    private static Block register(String path, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(FluidSieveMod.MOD_ID, path), block);
    }
}
