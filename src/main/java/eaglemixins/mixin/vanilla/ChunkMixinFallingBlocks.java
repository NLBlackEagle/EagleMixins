package eaglemixins.mixin.vanilla;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Chunk.class)
public abstract class ChunkMixinFallingBlocks {

    @WrapWithCondition(
            method = "setBlockState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBlockAdded(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V")
    )
    private boolean eaglemixins_vanillaChunk_setBlockState(Block instance, World world, BlockPos pos, IBlockState state) {
        return !(instance instanceof BlockFalling);
    }
}
