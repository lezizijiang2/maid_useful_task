package studio.fantasyit.maid_useful_task.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.maid_useful_task.MaidUsefulTask;
import studio.fantasyit.maid_useful_task.behavior.*;
import studio.fantasyit.maid_useful_task.memory.BlockValidationMemory;
import studio.fantasyit.maid_useful_task.util.MaidUtils;
import studio.fantasyit.maid_useful_task.util.MemoryUtil;
import studio.fantasyit.maid_useful_task.util.WrappedMaidFakePlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 女仆树木任务类
 * 负责女仆砍伐树木、种植树苗等林业相关工作
 */
public class MaidTreeTask implements IMaidTask, IMaidBlockPlaceTask, IMaidBlockDestroyTask, IMaidBlockUpTask {
    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(MaidUsefulTask.MODID, "maid_tree");
    }

    /**
     * 获取任务图标，使用橡树树苗作为图标
     */
    @Override
    public ItemStack getIcon() {
        return Items.OAK_SAPLING.getDefaultInstance();
    }

    /**
     * 是否启用四处张望和随机行走AI
     */
    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return true;
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(EntityMaid entityMaid) {
        return null;
    }

    /**
     * 判断是否应该破坏指定方块
     * @param maid 女仆实体
     * @param pos 方块位置
     * @return 如果是原木且属于有效的自然树木则返回true
     */
    @Override
    public boolean shouldDestroyBlock(EntityMaid maid, BlockPos pos) {
        if (MemoryUtil.getBlockUpContext(maid).hasTarget()) {
            if (pos.getY() < maid.getBlockY() && pos.getX() == maid.getBlockX() && pos.getZ() == maid.getBlockZ()) {
                return false;
            }
        }
        BlockState blockState = maid.level().getBlockState(pos);
        return blockState.is(BlockTags.LOGS) && isValidNatureTree(maid, pos);
    }

    /**
     * 判断是否有可能破坏指定方块
     * @param maid 女仆实体
     * @param pos 方块位置
     * @return 如果是原木或树叶则返回true
     */
    @Override
    public boolean mayDestroy(EntityMaid maid, BlockPos pos) {
        if (MemoryUtil.getBlockUpContext(maid).hasTarget()) {
            if (pos.getY() < maid.getBlockY() && pos.getX() == maid.getBlockX() && pos.getZ() == maid.getBlockZ()) {
                return false;
            }
        }
        BlockState blockState = maid.level().getBlockState(pos);
        if (blockState.is(BlockTags.LEAVES)) {
            return true;
        }
        return blockState.is(BlockTags.LOGS);
    }

    /**
     * 判断物品是否可以放置
     * @param maid 女仆实体
     * @param itemStack 物品堆叠
     * @return 如果是树苗则返回true
     */
    @Override
    public boolean shouldPlaceItemStack(EntityMaid maid, ItemStack itemStack) {
        return itemStack.is(ItemTags.SAPLINGS);
    }

    /**
     * 判断是否可以在指定位置放置物品
     * @param maid 女仆实体
     * @param itemStack 物品堆叠
     * @param pos 位置
     * @return 如果满足树苗种植条件则返回true
     */
    @Override
    public boolean shouldPlacePos(EntityMaid maid, ItemStack itemStack, BlockPos pos) {
        ServerLevel level = (ServerLevel) maid.level();
        if (!level.getBlockState(pos.below()).is(BlockTags.DIRT)) {
            return false;
        }
        if (!level.getBlockState(pos).canBeReplaced()) {
            return false;
        }
        final int[] dv = {0, 1, -1, 2, -2};
        for (int dx : dv) {
            for (int dy = 0; dy < 4; dy++) {
                for (int dz : dv) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    if (!level.getBlockState(pos.offset(dx, dy, dz)).canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 尝试取出工具（斧头）
     * @param maid 女仆实体
     */
    @Override
    public void tryTakeOutTool(EntityMaid maid) {
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getStackInSlot(i).is(ItemTags.AXES)) {
                @NotNull ItemStack tmp = inv.getStackInSlot(i);
                inv.setStackInSlot(i, maid.getMainHandItem());
                maid.setItemInHand(InteractionHand.MAIN_HAND, tmp);
                return;
            }
        }
    }

    /**
     * 切换到剪刀或空手
     * @param maid 女仆实体
     */
    public void swapShearsOrNone(EntityMaid maid) {
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        int target = -1;
        for (int i = 0; i < inv.getSlots(); i++) {
            if (inv.getStackInSlot(i).is(Items.SHEARS) || inv.getStackInSlot(i).is(ItemTags.HOES)) {
                target = i;
                break;
            }
            if (!inv.getStackInSlot(i).isDamageableItem()) {
                target = i;
            }
        }
        if (target != -1) {
            @NotNull ItemStack tmp = inv.getStackInSlot(target);
            inv.setStackInSlot(target, maid.getMainHandItem());
            maid.setItemInHand(InteractionHand.MAIN_HAND, tmp);
        }
    }

    /**
     * 根据目标方块类型取出相应的工具
     * @param maid 女仆实体
     * @param pos 目标方块位置
     */
    @Override
    public void tryTakeOutToolForTarget(EntityMaid maid, BlockPos pos) {
        if (maid.level().getBlockState(pos).is(BlockTags.LEAVES)) {
            swapShearsOrNone(maid);
        } else {
            tryTakeOutTool(maid);
        }
    }

    /**
     * 判断是否可以获取掉落物
     */
    @Override
    public boolean availableToGetDrop(EntityMaid maid, WrappedMaidFakePlayer fakePlayer, BlockPos pos, BlockState targetBlockState) {
        if (targetBlockState.is(BlockTags.LEAVES)) {
            return true;
        }
        return IMaidBlockDestroyTask.super.availableToGetDrop(maid, fakePlayer, pos, targetBlockState);
    }

    /**
     * 判断是否是有效的自然生成树木
     * @param maid 女仆实体
     * @param startPos 起始位置
     * @return 如果是自然生成的树木则返回true
     */
    protected boolean isValidNatureTree(EntityMaid maid, BlockPos startPos) {
        return isValidNatureTree(maid, startPos, new HashSet<>());
    }

    /**
     * 递归判断是否是有效的自然生成树木
     * @param maid 女仆实体
     * @param startPos 起始位置
     * @param visited 已访问的位置集合（防止循环引用）
     * @return 如果是自然生成的树木则返回true
     */
    protected boolean isValidNatureTree(EntityMaid maid, BlockPos startPos, Set<BlockPos> visited) {
        BlockValidationMemory validationMemory = MemoryUtil.getBlockValidationMemory(maid);
        if (validationMemory.hasRecord(startPos)) {
            return validationMemory.isValid(startPos, false);
        }
        if (visited.contains(startPos)) {
            return false;
        }
        visited.add(startPos);
        boolean valid = false;
        final int[] dv = {0, 1, -1};
        for (int dx : dv) {
            for (int dz : dv) {
                for (int dy : dv) {
                    BlockPos offset = startPos.offset(dx, dy, dz);
                    BlockState blockState = maid.level().getBlockState(offset);
                    if (blockState.is(BlockTags.LEAVES) && !blockState.getValue(LeavesBlock.PERSISTENT)) {
                        valid = true;
                    }
                    if (blockState.is(BlockTags.LOGS) && isValidNatureTree(maid, offset, visited)) {
                        valid = true;
                    }
                }
            }
        }
        if (valid) {
            validationMemory.setValid(startPos);
        } else {
            validationMemory.setInvalid(startPos);
        }
        return valid;
    }

    /**
     * 创建女仆AI行为控制列表
     * @param entityMaid 女仆实体
     * @return 行为控制列表
     */
    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid entityMaid) {
        ArrayList<Pair<Integer, BehaviorControl<? super EntityMaid>>> list = new ArrayList<>();

        // 破坏方块行为
        list.add(Pair.of(1, new DestoryBlockBehavior()));
        list.add(Pair.of(1, new DestoryBlockMoveBehavior()));

        // 向上放置/破坏方块行为
        list.add(Pair.of(2, new BlockUpScheduleBehavior()));
        list.add(Pair.of(2, new BlockUpPlaceBehavior()));
        list.add(Pair.of(2, new BlockUpDestroyBehavior()));

        // 放置方块行为
        list.add(Pair.of(3, new PlaceBlockBehavior()));
        list.add(Pair.of(3, new PlaceBlockMoveBehavior()));

        // 更新方块验证内存
        list.add(Pair.of(4, new UpdateValidationMemoryBehavior()));

        return list;
    }

    /**
     * 判断物品堆叠是否有效
     */
    @Override
    public boolean isValidItemStack(EntityMaid maid, ItemStack stack) {
        return stack.is(ItemTags.LOGS);
    }

    /**
     * 判断是否是破坏工具
     */
    @Override
    public boolean isDestroyTool(EntityMaid maid, ItemStack stack) {
        return stack.is(ItemTags.AXES);
    }

    /**
     * 判断是否应该查找指定方块
     */
    @Override
    public boolean isFindingBlock(EntityMaid maid, BlockPos target, BlockPos standPos) {
        if (target.distSqr(standPos) > touchLimit() * touchLimit()) {
            return false;
        }
        return maid.level().getBlockState(target).is(BlockTags.LOGS) && isValidNatureTree(maid, target);
    }

    /**
     * 尝试向上破坏方块
     */
    @Override
    public boolean tryDestroyBlockUp(EntityMaid maid, BlockPos targetPos) {
        return tryDestroyBlock(maid, targetPos);
    }

    /**
     * 尝试放置方块
     */
    @Override
    public boolean tryPlaceBlock(EntityMaid maid, BlockPos pos) {
        if (IMaidBlockPlaceTask.super.tryPlaceBlock(maid, pos)) {
            MemoryUtil.getBlockValidationMemory(maid).setValid(pos);
            return true;
        }
        return false;
    }

    /**
     * 尝试破坏方块
     */
    @Override
    public boolean tryDestroyBlock(EntityMaid maid, BlockPos blockPos) {
        if (MaidUtils.destroyBlock(maid, blockPos)) {
            MemoryUtil.getBlockValidationMemory(maid).remove(blockPos);
            return true;
        }
        return false;
    }
}
