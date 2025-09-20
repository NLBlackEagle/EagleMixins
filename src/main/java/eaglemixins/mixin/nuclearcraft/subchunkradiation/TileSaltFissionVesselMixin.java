package eaglemixins.mixin.nuclearcraft.subchunkradiation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eaglemixins.EagleMixins;
import eaglemixins.capability.ChunkRadiationSource;
import nc.capability.radiation.source.IRadiationSource;
import nc.multiblock.saltFission.tile.TileSaltFissionVessel;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TileSaltFissionVessel.class)
public abstract class TileSaltFissionVesselMixin extends TileEntity {
    @WrapOperation(
            method = "doMeltdown",
            at = @At(value = "INVOKE", target = "Lnc/radiation/RadiationHelper;addToSourceRadiation(Lnc/capability/radiation/source/IRadiationSource;D)V"),
            remap = false
    )
    private void eaglemixins_useSubchunkRadiation(IRadiationSource source, double addedRadiation, Operation<Void> original){
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

        chunkSource.setSubchunk(this.pos);
        original.call(source, addedRadiation);
        chunkSource.resetSubchunk();
    }
}
