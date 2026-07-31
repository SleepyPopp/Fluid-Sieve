package net.crioch.fluid_sieve.loot.context;

import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class FluidSieveLootContextTypes {
    private static final LootContextTypesMapGetter ORIGINAL_MAP = ((LootContextTypesMapGetter)(Object)new LootContextParamSets());
    public static final ContextKeySet FLUID_SIEVE = register("sieve", builder -> builder.required(LootContextParams.BLOCK_STATE).required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY));

    private static ContextKeySet register(String name, Consumer<ContextKeySet.Builder> type) {
        ContextKeySet.Builder builder = new ContextKeySet.Builder();
        type.accept(builder);
        ContextKeySet lootContextType = builder.build();
        Identifier identifier = Identifier.parse(name);
        ContextKeySet lootContextType2 = ORIGINAL_MAP.getMap().put(identifier, lootContextType);
        if (lootContextType2 != null) {
            throw new IllegalStateException("Loot table parameter set " + String.valueOf(identifier) + " is already registered");
        } else {
            return lootContextType;
        }
    }
}
