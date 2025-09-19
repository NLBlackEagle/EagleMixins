package eaglemixins.mixin.nuclearcraft;

import nc.capability.radiation.source.IRadiationSource;
import nc.radiation.RadSources;
import nc.radiation.RadiationHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

/**
 * Corium-style radiation for any fluid whose FLUID_MAP entry (from S:radiation_fluids)
 * is > 0. No hardcoded block IDs; we key off the fluid's registry *name*.
 */
@Mixin(value = BlockFluidClassic.class)
public abstract class MixinBlockFluidClassicRadiation {

    @Inject(method = {"updateTick"}, at = @At("TAIL"), require = 0)
    private void eagleMixins$radiateConfiguredFluids(World world, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        if (world.isRemote) return;

        Block self = (Block) (Object) this;
        if (!(self instanceof IFluidBlock)) return;

        Fluid fluid = ((IFluidBlock) self).getFluid();
        String fluidName = (fluid != null ? fluid.getName() : null);
        if (fluidName == null) return;

        double basePerTick = RadSources.FLUID_MAP.getDouble(fluidName);
        if (basePerTick <= 0.0D) return;

        double scale = 1.0D;
        if (self instanceof BlockFluidBase) {
            scale = ((BlockFluidBase) self).getQuantaPercentage(world, pos);
        }
        double radsPerTick = basePerTick * scale;
        if (radsPerTick <= 0.0D) return;

        Chunk chunk = world.getChunk(pos);
        if (chunk == null || !chunk.isLoaded()) return;

        IRadiationSource src = RadiationHelper.getRadiationSource(chunk);
        if (src == null) return;

        RadiationHelper.addToSourceRadiation(src, radsPerTick);

    }
}
