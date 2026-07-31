package net.crioch.fluid_sieve.block;

import net.crioch.fluid_sieve.loot.context.FluidSieveLootContextTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.Container;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.util.context.ContextMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;

import java.util.Iterator;
import java.util.List;

public class BaseSieve extends Block implements SimpleWaterloggedBlock {
    private static final VoxelShape selectionShape = Shapes.or(
            box(0, 0, 0, 1, 16, 1),
            box(15, 0, 0, 16, 16, 1),
            box(0, 0, 15, 1, 16, 16),
            box(15, 0, 15, 16, 16, 16),
            box(1, 1, 0, 15, 14, 1),
            box(1, 1, 15, 15, 14, 16),
            box(0, 1, 1, 1, 14, 16),
            box(15, 1, 1, 16, 14, 16)
    );

    public BaseSieve(Properties settings, Identifier key) {
        super(settings.randomTicks().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, key)));
        this.registerDefaultState(
                this.getStateDefinition()
                        .any()
                        .setValue(BlockStateProperties.WATERLOGGED, false)
        );
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        boolean waterlogged = state.getValue(BlockStateProperties.WATERLOGGED);

        Identifier id = BuiltInRegistries.FLUID.getKey(waterlogged ? Fluids.WATER.getSource() : Fluids.EMPTY);

        List<ItemStack> loot = getLoot(id, world, state, pos, random);

        if (!loot.isEmpty()) {
            // Get the block entity below the sieve
            BlockPos blockPos = pos.below();
            BlockEntity blockEntity = world.getBlockEntity(blockPos);

            if (blockEntity instanceof Container inventory) {
                int inventorySize = inventory.getContainerSize();
                boolean inventoryChanged = false;

                Iterator<ItemStack> iterator = loot.iterator();
                int firstEmptySlot = 0;
                while (iterator.hasNext() && firstEmptySlot < inventorySize) {
                    ItemStack stack = iterator.next();
                    int initialCount = stack.getCount();
                    firstEmptySlot = insert(stack, inventory, firstEmptySlot);
                    if (stack.isEmpty()) {
                        inventoryChanged = true;
                        iterator.remove();
                    } else if (initialCount - stack.getCount() > 0) {
                        inventoryChanged = true;
                    }
                }

                if (inventoryChanged) {
                    inventory.setChanged();
                }

                if (!loot.isEmpty()) {
                    spawnStacksInWorld(world, pos, loot);
                }
            } else {
                spawnStacksInWorld(world, pos, loot);
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockState downState = world.getBlockState(pos.below());
        return downState.isFaceSturdy(world, pos.above(), Direction.UP, SupportType.FULL) || downState.is(Blocks.HOPPER);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (this.canSurvive(state, world, pos)) {
            return state;
        }

        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos blockPos = ctx.getClickedPos();
        FluidState fluidState = ctx.getLevel().getFluidState(blockPos);
        return this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.WATERLOGGED);
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context) {
        return selectionShape;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return selectionShape;
    }

    private List<ItemStack> getLoot(Identifier fluidId, ServerLevel world, BlockState state, BlockPos pos, RandomSource random) {
        Identifier path = fluidId.withPrefix("sieve/");
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, path);

        LootTable lootTable = world.getServer().reloadableRegistries().getLootTable(key);

        // Exit early if the loot table isn't defined
        if (lootTable.equals(LootTable.EMPTY)) {
            return List.of();
        }

        net.minecraft.util.context.ContextMap.Builder builder = new net.minecraft.util.context.ContextMap.Builder()
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));


        // Get all Entities within the sieve
        List<? extends Entity> entitiesWithinBlock = world.getEntities(EntityTypeTest.forClass(Entity.class), (livingEntity -> livingEntity.blockPosition().equals(pos)));

        // If any are within it, add a random one as the 'this' entity for the loot table
        if (!entitiesWithinBlock.isEmpty()) {

            builder.withOptionalParameter(LootContextParams.THIS_ENTITY, entitiesWithinBlock.get(random.nextInt(entitiesWithinBlock.size())));
        }

        ContextMap map = builder.create(FluidSieveLootContextTypes.FLUID_SIEVE);

        LootParams context = new LootParams(world, map, null, 0);

        return lootTable.getRandomItems(context);
    }

    private static void spawnStacksInWorld(ServerLevel world, BlockPos pos, List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            Block.popResource(world, pos, stack);
        }
    }

    private static int insert(ItemStack stack, Container inventory, int firstEmptySlot) {
        if (stack.isStackable()) {
            for (int slotIndex = firstEmptySlot; slotIndex < inventory.getContainerSize(); slotIndex++) {
                ItemStack slot = inventory.getItem(slotIndex);
                if (slot.is(stack.getItem())) {
                    int maxStack = Math.min(slot.getMaxStackSize(), inventory.getMaxStackSize());
                    int amount = Math.min(maxStack - slot.getCount(), stack.getCount());
                    slot.grow(amount);
                    inventory.setItem(slotIndex, slot);
                    stack.shrink(amount);
                } else if (slot.isEmpty()) {
                    inventory.setItem(slotIndex, stack.copy());
                    stack.shrink(stack.getCount());
                }

                if (firstEmptySlot - slotIndex == 0 && slot.getMaxStackSize() - slot.getCount() == 0) {
                    firstEmptySlot++;
                }

                if (stack.isEmpty()) {
                    break;
                }
            }
        } else {
            for (int slotIndex = firstEmptySlot; slotIndex < inventory.getContainerSize(); slotIndex++) {
                ItemStack slot = inventory.getItem(slotIndex);
                if (slot.isEmpty()) {
                    inventory.setItem(slotIndex, stack.split(1));
                    if (firstEmptySlot - slotIndex == 0) {
                        firstEmptySlot++;
                    }
                    if (stack.isEmpty()) {
                        break;
                    }
                }
            }
        }

        return firstEmptySlot;
    }
}
