package studio.fantasyit.maid_useful_task.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.maid_useful_task.MaidUsefulTask;
import studio.fantasyit.maid_useful_task.memory.*;

import java.util.Optional;

public class MemoryModuleRegistry {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES
            = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, MaidUsefulTask.MODID);
            
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BlockTargetMemory>> DESTROY_TARGET
            = MEMORY_MODULE_TYPES.register("block_targets", () -> new MemoryModuleType<>(Optional.of(BlockTargetMemory.CODEC)));
            
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BlockPos>> PLACE_TARGET
            = MEMORY_MODULE_TYPES.register("place_target", () -> new MemoryModuleType<>(Optional.empty()));
            
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BlockUpContext>> BLOCK_UP_TARGET
            = MEMORY_MODULE_TYPES.register("block_up", () -> new MemoryModuleType<>(Optional.of(BlockUpContext.CODEC)));
            
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BlockValidationMemory>> BLOCK_VALIDATION
            = MEMORY_MODULE_TYPES.register("block_validation", () -> new MemoryModuleType<>(Optional.of(BlockValidationMemory.CODEC)));
            
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<CurrentWork>> CURRENT_WORK 
            = MEMORY_MODULE_TYPES.register("current_work", () -> new MemoryModuleType<>(Optional.empty()));

    public static void register(IEventBus eventBus) {
        MEMORY_MODULE_TYPES.register(eventBus);
    }
}
