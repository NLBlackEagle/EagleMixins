package eaglemixins.mixin.nuclearcraft;

import eaglemixins.EagleMixins;
import nc.capability.radiation.source.IRadiationSource;
import nc.radiation.RadiationHelper;
import nc.radiation.RadSources;
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
 * Corium-style radiation for specific fluids by injecting into the shared
 * BlockFluidClassic#updateTick that the target fluid blocks inherit.
 */
@Mixin(value = BlockFluidClassic.class)
public abstract class MixinBlockFluidClassicRadiation {

    @Inject(method = {"updateTick" }, at = @At("TAIL"), require = 0)
    private void eagleMixins$radiateSelectedFluids(World world, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        if (world.isRemote) return;

        Block self = (Block) (Object) this;
        if (!(self instanceof IFluidBlock)) return;

        String id = String.valueOf(self.getRegistryName());
        boolean target =
                "contenttweaker:coolant_fluid".equals(id) ||
                        "lycanitesmobs:acid".equals(id) ||
                        "lycanitesmobs:sharacid".equals(id);
        if (!target) return;

        Fluid fluid = ((IFluidBlock) self).getFluid();
        String fluidName = fluid != null ? fluid.getName() : null;
        if (fluidName == null) return;

        double base = RadSources.FLUID_MAP.getDouble(fluidName);
        if (base <= 0.0D) {
            if (base <= 0.0D) return;
        }

        // Scale by quanta (how "full" the fluid block is)
        double scale = 1.0D;
        if (self instanceof BlockFluidBase) {
            scale = ((BlockFluidBase) self).getQuantaPercentage(world, pos);
        }
        double radsPerTick = base * scale;
        if (radsPerTick <= 0.0D) return;

        Chunk chunk = world.getChunk(pos);
        if (chunk == null || !chunk.isLoaded()) return;

        IRadiationSource src = RadiationHelper.getRadiationSource(chunk);
        if (src == null) return;

        RadiationHelper.addToSourceRadiation(src, radsPerTick);

        // Debug; trim later
        EagleMixins.LOGGER.info("[EagleMixins] +{} rads/t from {} (fluid='{}', scale={}) @ {} (chunk {},{})",
                radsPerTick, id, fluidName, scale, pos, chunk.x, chunk.z);
    }
}
