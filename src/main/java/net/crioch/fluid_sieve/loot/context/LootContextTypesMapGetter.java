package net.crioch.fluid_sieve.loot.context;

import com.google.common.collect.BiMap;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.resources.Identifier;

public interface LootContextTypesMapGetter {
    BiMap<Identifier, ContextKeySet> getMap();
}
