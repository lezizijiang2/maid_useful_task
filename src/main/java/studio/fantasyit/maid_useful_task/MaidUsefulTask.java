package studio.fantasyit.maid_useful_task;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import studio.fantasyit.maid_useful_task.registry.MemoryModuleRegistry;

/**
 * 女仆实用任务模组主类
 * 负责模组初始化和注册各种组件
 */
@Mod(MaidUsefulTask.MODID)
public class MaidUsefulTask {

    /**
     * 定义模组ID常量，作为全局引用
     */
    public static final String MODID = "maid_useful_task";

    /**
     * 模组构造函数
     * 负责注册各种事件总线和模组组件
     */
    public MaidUsefulTask(IEventBus modEventBus) {
        // 注册记忆模块
        MemoryModuleRegistry.register(modEventBus);
    }
}
