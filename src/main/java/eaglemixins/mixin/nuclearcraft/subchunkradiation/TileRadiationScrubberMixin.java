package eaglemixins.mixin.nuclearcraft.subchunkradiation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eaglemixins.EagleMixins;
import eaglemixins.capability.ChunkRadiationSource;
import nc.capability.radiation.source.IRadiationSource;
import nc.tile.radiation.TileRadiationScrubber;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TileRadiationScrubber.class)
public abstract class TileRadiationScrubberMixin extends TileEntity {
    @WrapOperation(
            method = "getRawScrubberRate",
            at = {
                    @At(value = "INVOKE", target = "Lnc/capability/radiation/source/IRadiationSource;getEffectiveScrubberCount()D"),
                    @At(value = "INVOKE", target = "Lnc/capability/radiation/source/IRadiationSource;getScrubbingFraction()D")
            },
            remap = false
    )
    private double eaglemixins_useSubchunkRadiation(IRadiationSource source, Operation<Double> original){
        if(!(source instanceof ChunkRadiationSource)){
            EagleMixins.LOGGER.error("EagleMixins subchunk radiation capability not a ChunkRadiationSoruce in TileRadiationScrubber! Notify nischhelm!");
            return original.call(source);
        }

        ChunkRadiationSource chunkSource = (ChunkRadiationSource) source;
        if(!chunkSource.subchunkIsReset()){
            EagleMixins.LOGGER.error("EagleMixins subchunk radiation subchunk not reset in TileRadiationScrubber! Notify nischhelm!");
            return original.call(source);
        }

        chunkSource.setSubchunk(pos);
        double retValue = original.call(source);
        chunkSource.resetSubchunk();
        return retValue;
    }
}
