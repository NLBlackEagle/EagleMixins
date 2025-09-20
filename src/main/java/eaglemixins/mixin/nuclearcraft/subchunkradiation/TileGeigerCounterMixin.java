package eaglemixins.mixin.nuclearcraft.subchunkradiation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eaglemixins.EagleMixins;
import eaglemixins.capability.ChunkRadiationSource;
import nc.capability.radiation.source.IRadiationSource;
import nc.tile.radiation.TileGeigerCounter;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TileGeigerCounter.class)
public abstract class TileGeigerCounterMixin extends TileEntity {
    @WrapOperation(
            method = "getChunkRadiationLevel()D",
            at = @At(value = "INVOKE", target = "Lnc/capability/radiation/source/IRadiationSource;getRadiationLevel()D"),
            remap = false
    )
    private double eaglemixins_useSubchunkRadiation(IRadiationSource source, Operation<Double> original){
        if(!(source instanceof ChunkRadiationSource)){
            EagleMixins.LOGGER.error("EagleMixins subchunk radiation capability not a ChunkRadiationSoruce in TileSaltFissionVessel! Notify nischhelm!");
            return original.call(source);
        }

        ChunkRadiationSource chunkSource = (ChunkRadiationSource) source;
        if(!chunkSource.subchunkIsReset()){
            EagleMixins.LOGGER.error("EagleMixins subchunk radiation subchunk not reset in TileSaltFissionVessel! Notify nischhelm!");
            original.call(source);
            return 0;
        }

        chunkSource.setSubchunk(pos);
        original.call(source);
        chunkSource.resetSubchunk();
        return 0;
    }
}
