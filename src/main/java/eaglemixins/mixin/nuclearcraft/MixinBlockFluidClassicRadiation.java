package eaglemixins.mixin.nuclearcraft;

import com.llamalad7.mixinextras.sugar.Local;
import eaglemixins.capability.ChunkRadiationSource;
import nc.capability.radiation.source.IRadiationSource;
import nc.radiation.RadSources;
import nc.radiation.RadiationHelper;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

/**
 * Corium-style radiation for any fluid whose FLUID_MAP entry (from S:radiation_fluids)
 * is > 0. No hardcoded block IDs; we key off the fluid's registry *name*.
 */
@Mixin(BlockFluidClassic.class)
public abstract class MixinBlockFluidClassicRadiation extends BlockFluidBase {
    public MixinBlockFluidClassicRadiation(Fluid fluid, Material material, MapColor mapColor) {
        super(fluid, material, mapColor);
    }

    @Inject(method = "updateTick", at = @At("TAIL"))
    private void eagleMixins$radiateConfiguredFluids(World world, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci, @Local(name = "quantaRemaining") int quantaRemaining) {
        if (world.isRemote) return;

        Fluid fluid = this.getFluid();
        String fluidName = (fluid != null ? fluid.getName() : null);
        if (fluidName == null || fluidName.equals("corium")) return;

        double basePerTick = RadSources.FLUID_MAP.getDouble(fluidName) / RadSources.FLUID;
        if (basePerTick <= 0.0D) return;

        double radsPerTick = basePerTick * quantaRemaining;
        if (radsPerTick <= 0.0D) return;

        Chunk chunk = world.getChunk(pos);
        if (!chunk.isLoaded()) return;

        IRadiationSource src = RadiationHelper.getRadiationSource(chunk);
        if (!(src instanceof ChunkRadiationSource)) return;
        ChunkRadiationSource chunkRadSource = (ChunkRadiationSource) src;

        chunkRadSource.setSubchunk(pos);
        RadiationHelper.addToSourceRadiation(src, radsPerTick);
        chunkRadSource.resetSubchunk();
    }
}
