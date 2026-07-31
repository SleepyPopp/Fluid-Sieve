package net.crioch.fluid_sieve.mixin;

import com.google.common.collect.BiMap;
import net.crioch.fluid_sieve.loot.context.LootContextTypesMapGetter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LootContextParamSets.class)
public class LootContextParamSetsMixin implements LootContextTypesMapGetter {
    @Shadow
    @Final
    private static BiMap<Identifier, ContextKeySet> REGISTRY;

    @Override
    public BiMap<Identifier, ContextKeySet> getMap() {
        return REGISTRY;
    }
}
