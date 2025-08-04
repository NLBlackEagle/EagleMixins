package eaglemixins.mixin.vanilla;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockTrapDoor.class)
public abstract class TrapdoorLadderMixin {

    @Inject(method = "isLadder", at = @At("HEAD"), cancellable = true, remap = false)
    private void eaglemixins$expandLadderSupport(
            IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!state.getValue(BlockTrapDoor.OPEN)) return;

        IBlockState down = world.getBlockState(pos.down());
        Block downBlock = down.getBlock();
        ResourceLocation id = downBlock.getRegistryName();

        // Must exist and end with "_ladder"
        if (id != null && id.getPath().endsWith("_ladder")) {
            // Try to match facing if possible (only if it's a vanilla-style ladder)
            if (downBlock instanceof BlockLadder) {
                EnumFacing downFacing = down.getValue(BlockLadder.FACING);
                EnumFacing trapdoorFacing = state.getValue(BlockTrapDoor.FACING);

                if (downFacing == trapdoorFacing) {
                    cir.setReturnValue(true);
                }
            } else {
                // No FACING property? Assume climbable
                cir.setReturnValue(true);
            }
        }
    }
}