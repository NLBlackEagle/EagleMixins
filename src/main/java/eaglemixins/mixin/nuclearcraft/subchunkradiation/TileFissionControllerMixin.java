package eaglemixins.mixin.nuclearcraft.subchunkradiation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eaglemixins.EagleMixins;
import eaglemixins.capability.ChunkRadiationSource;
import nc.capability.radiation.source.IRadiationSource;
import nc.tile.generator.TileFissionController;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TileFissionController.class)
public abstract class TileFissionControllerMixin {
    @WrapOperation(
            method = "meltdown",
            at = @At(value = "INVOKE", target = "Lnc/radiation/RadiationHelper;addToSourceRadiation(Lnc/capability/radiation/source/IRadiationSource;D)V"),
            remap = false
    )
    private void eaglemixins_useSubchunkRadiation(IRadiationSource source, double addedRadiation, Operation<Void> original, @Local(name = "middle") BlockPos middle){
        if(!(source instanceof ChunkRadiationSource)){
            EagleMixins.LOGGER.error("EagleMixins subchunk radiation capability not a ChunkRadiationSoruce in TileSaltFissionVessel! Notify nischhelm!");
            original.call(source, addedRadiation);
            return;
        }

        ChunkRadiationSource chunkSource = (ChunkRadiationSource) source;
        if(!chunkSource.subchunkIsReset()){
            EagleMixins.LOGGER.error("EagleMixins subchunk radiation subchunk not reset in TileSaltFissionVessel! Notify nischhelm!");
            original.call(source, addedRadiation);
            return;
        }

        chunkSource.setSubchunk(middle);
        original.call(source, addedRadiation);
        chunkSource.resetSubchunk();
    }
}
