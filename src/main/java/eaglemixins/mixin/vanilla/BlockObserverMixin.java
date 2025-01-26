package eaglemixins.mixin.vanilla;

import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.block.BlockObserver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockObserver.class)
public abstract class BlockObserverMixin {
	
	@Shadow protected abstract void updateNeighborsInFront(World worldIn, BlockPos pos, IBlockState state);
	
	/**
	 * Observer fix thanks to UniversalTweaks, WaitingIdly, RandomPatches
	 */
	@Inject(
			method = "onBlockAdded",
			at = @At("HEAD"),
			cancellable = true
	)
	private void eagleMixins_vanillaBlockObserver_onBlockAdded(World world, BlockPos pos, IBlockState state, CallbackInfo ci) {
		if(!ForgeConfigHandler.server.patchObserversTickingOnWorldGen) return;
		if(!world.isRemote) {
			if(state.getValue(BlockObserver.POWERED) && !world.isUpdateScheduled(pos, ((BlockObserver)(Object)this))) {
				IBlockState unpowered = state.withProperty(BlockObserver.POWERED, false);
				world.setBlockState(pos, unpowered, 18);
				this.updateNeighborsInFront(world, pos, state);
			}
		}
		ci.cancel();
	}
}