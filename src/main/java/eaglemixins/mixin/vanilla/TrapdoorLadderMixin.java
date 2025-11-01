package eaglemixins.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockTrapDoor.class)
public abstract class TrapdoorLadderMixin {

    @ModifyReturnValue(method = "isLadder", at = @At(value = "RETURN", ordinal = 1), remap = false)
    private boolean eaglemixins_expandLadderSupport(boolean originalReturnValue, IBlockState trapDoorState, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        if (!trapDoorState.getValue(BlockTrapDoor.OPEN)) return originalReturnValue;

        IBlockState downState = world.getBlockState(pos.down());
        Block downBlock = downState.getBlock();
        if(downBlock == Blocks.LADDER) return originalReturnValue;

        if (downBlock.isLadder(downState, world, pos, entity)) { //in RLCraft this is for Quark iron ladder, BetterNether reeds ladder and vanilla vines
            // Try to match facing if possible (only if it's a vanilla-style ladder)
            if (downBlock instanceof BlockLadder) {
                return downState.getValue(BlockLadder.FACING) == trapDoorState.getValue(BlockTrapDoor.FACING);
            } else {
                // Not inheriting from ladder but below is climbable -> Assume trapdoor climbable as well
                return true;
            }
        }
        return false;
    }
}