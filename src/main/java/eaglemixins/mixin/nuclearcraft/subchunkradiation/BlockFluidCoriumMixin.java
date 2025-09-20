package eaglemixins.mixin.nuclearcraft.subchunkradiation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eaglemixins.EagleMixins;
import eaglemixins.capability.ChunkRadiationSource;
import nc.block.fluid.BlockFluidCorium;
import nc.capability.radiation.source.IRadiationSource;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nonnull;

@Mixin(BlockFluidCorium.class)
public abstract class BlockFluidCoriumMixin extends BlockFluidBase {
    public BlockFluidCoriumMixin(Fluid fluid, Material material, MapColor mapColor) {
        super(fluid, material, mapColor);
    }

    @WrapOperation(
            method = "updateTick",
            at = @At(value = "INVOKE", target = "Lnc/radiation/RadiationHelper;addToSourceRadiation(Lnc/capability/radiation/source/IRadiationSource;D)V", remap = false)
    )
    private void eaglemixins_setCurrentSubChunk(IRadiationSource source, double addedRadiation, Operation<Void> original, @Nonnull World world, @Nonnull BlockPos pos){
        if(!(source instanceof ChunkRadiationSource)){
            EagleMixins.LOGGER.error("EagleMixins subchunk radiation capability not a ChunkRadiationSoruce in BlockFluidCorium! Notify nischhelm!");
            original.call(source, addedRadiation);
            return;
        }

        ChunkRadiationSource chunkSource = (ChunkRadiationSource) source;
        if(!chunkSource.subchunkIsReset()){
            EagleMixins.LOGGER.error("EagleMixins subchunk radiation subchunk not reset in BlockFluidCorium! Notify nischhelm!");
            original.call(source, addedRadiation);
            return;
        }

        chunkSource.setSubchunk(MathHelper.clamp(pos.getY() >> 4, 0, 15));
        original.call(source, addedRadiation);
        chunkSource.resetSubchunk();
    }
}
