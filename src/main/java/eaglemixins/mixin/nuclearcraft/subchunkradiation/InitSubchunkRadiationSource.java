package eaglemixins.mixin.nuclearcraft.subchunkradiation;

import eaglemixins.capability.ChunkRadiationSource;
import nc.capability.radiation.source.RadiationSource;
import nc.capability.radiation.source.RadiationSourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RadiationSourceProvider.class)
public abstract class InitSubchunkRadiationSource {
    @Redirect(
            method = "<init>",
            at = @At(value = "NEW", target = "(D)Lnc/capability/radiation/source/RadiationSource;"),
            remap = false
    )
    private RadiationSource eaglemixins_useChunkRadiationSource(double startRadiation){
        return new ChunkRadiationSource(startRadiation);
    }
}
