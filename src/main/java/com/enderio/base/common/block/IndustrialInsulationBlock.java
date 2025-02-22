package com.enderio.base.common.block;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.Tags;

import java.util.Queue;

public class IndustrialInsulationBlock extends SpongeBlock {
    private static final int MAX_REPLACES = 64;
    private static final int MAX_RANGE = 6;

    public IndustrialInsulationBlock(Properties props) {
        super(props);
    }

    private void removeBlock(BlockPos block, Level level) {
        level.setBlock(block, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void tryAbsorbWater(Level level, BlockPos pos) {
        Queue<Tuple<BlockPos, Integer>> queue = Lists.newLinkedList();
        queue.add(new Tuple<>(pos, 0));
        int checkedBlocksCount = 0;

        while (!queue.isEmpty()) {
            Tuple<BlockPos, Integer> tuple = queue.poll();
            BlockPos blockpos = tuple.getA();
            int currentRange = tuple.getB();

            for (Direction direction : Direction.values()) {
                BlockPos blockToCheckPos = blockpos.relative(direction);
                BlockState blockToCheckState = level.getBlockState(blockToCheckPos);
                Block blockToCheck = blockToCheckState.getBlock();
                Material blockToReplaceMaterial = blockToCheckState.getMaterial();

                if (blockToCheck instanceof BucketPickup bucketPickup && !bucketPickup.pickupBlock(level, blockToCheckPos, blockToCheckState).isEmpty()) {
                    ++checkedBlocksCount;

                    if (currentRange < MAX_RANGE) {
                        queue.add(new Tuple<>(blockToCheckPos, currentRange + 1));
                    }

                } else if (blockToCheckState.getBlock() instanceof LiquidBlock) {
                    if (blockToReplaceMaterial == Material.WATER_PLANT || blockToReplaceMaterial == Material.REPLACEABLE_WATER_PLANT) {
                        BlockEntity blockEntity = blockToCheckState.hasBlockEntity() ? level.getBlockEntity(blockToCheckPos) : null;
                        dropResources(blockToCheckState, level, blockToCheckPos, blockEntity);
                    }
                    this.removeBlock(blockToCheckPos, level);
                    ++checkedBlocksCount;

                    if (currentRange < MAX_RANGE) {
                        queue.add(new Tuple<>(blockToCheckPos, currentRange + 1));
                    }
                }
            }

            if (checkedBlocksCount > MAX_REPLACES) {
                break;
            }
        }
    }
}
