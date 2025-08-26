package eaglemixins.mixin.nuclearcraft;

import eaglemixins.EagleMixins;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.IFluidBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.Debug;  // <-- add this import

import java.util.Random;

@Debug(export = true)
@Mixin(value = WorldServer.class, remap = true)
public abstract class MixinTickUpdatesHook {

    @Redirect(
            method = "tickUpdates",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V",
                    remap = true
            )
    )
    private void eagleMixins$wrapUpdateTick(Block block, World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote && block instanceof IFluidBlock) {
            EagleMixins.LOGGER.info("[EagleMixins] Fluid scheduled tick: {} @ {}", block.getRegistryName(), pos);
        }

        block.updateTick(world, pos, state, rand);
    }
}
