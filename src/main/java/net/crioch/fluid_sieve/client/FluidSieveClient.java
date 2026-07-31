package net.crioch.fluid_sieve.client;

import net.crioch.fluid_sieve.block.FluidSieveBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public class FluidSieveClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, FluidSieveBlocks.STRING_SIEVE, FluidSieveBlocks.DENSE_SIEVE);
    }
}