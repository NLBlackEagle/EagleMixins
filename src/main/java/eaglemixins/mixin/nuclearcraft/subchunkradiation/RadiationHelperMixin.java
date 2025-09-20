package eaglemixins.mixin.nuclearcraft.subchunkradiation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eaglemixins.EagleMixins;
import eaglemixins.capability.ChunkRadiationSource;
import nc.capability.radiation.source.IRadiationSource;
import nc.config.NCConfig;
import nc.radiation.RadiationHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RadiationHelper.class)
public abstract class RadiationHelperMixin {
    @Shadow(remap = false) public static IRadiationSource getRadiationSource(ICapabilityProvider provider) {return null;}

    @WrapOperation(
            method = "transferRadsToPlayer",
            at = @At(value = "INVOKE", target = "Lnc/capability/radiation/source/IRadiationSource;getRadiationLevel()D"),
            remap = false
    )
    private static double eaglemixins_useSubchunkRadiation_transferRadsToPlayer(IRadiationSource instance, Operation<Double> original, @Local(argsOnly = true) EntityPlayer player) {
        if (instance instanceof ChunkRadiationSource)
            return ((ChunkRadiationSource) instance).getSubchunkRadiationLevel(MathHelper.clamp(player.getPosition().getY() >> 4, 0, 15));

        EagleMixins.LOGGER.error("EagleMixins RadiationHelper.transferRadsToPlayer called for non ChunkRadiationSource. Notify nischhelm!");
        return original.call(instance);
    }

    /**
     * @author nischhelm
     * @reason easier with overwrite
     */
    @Overwrite(remap = false)
    public static void spreadRadiationFromChunk(Chunk chunk, Chunk targetChunk) {
        if (chunk == null || !chunk.isLoaded()) return;
        IRadiationSource chunkSource = getRadiationSource(chunk);
        if (!(chunkSource instanceof ChunkRadiationSource)) return;
        ChunkRadiationSource chunkRadSource = (ChunkRadiationSource) chunkSource;

        if (targetChunk != null && targetChunk.isLoaded()) {
            IRadiationSource targetChunkSource = getRadiationSource(targetChunk);
            if(targetChunkSource instanceof ChunkRadiationSource) {
                ChunkRadiationSource targetChunkRadSource = (ChunkRadiationSource) targetChunkSource;

                for(int subchunk = 0; subchunk < 16; subchunk++) {
                    chunkRadSource.setSubchunk(subchunk);
                    targetChunkRadSource.setSubchunk(subchunk);

                    if (!chunkSource.isRadiationNegligible() && (targetChunkSource.getRadiationLevel() == 0 || chunkSource.getRadiationLevel() / targetChunkSource.getRadiationLevel() > 1 + NCConfig.radiation_spread_gradient)) {
                        double radiationSpread = (chunkSource.getRadiationLevel() - targetChunkSource.getRadiationLevel()) * NCConfig.radiation_spread_rate;
                        chunkSource.setRadiationLevel(chunkSource.getRadiationLevel() - radiationSpread);
                        targetChunkSource.setRadiationLevel(targetChunkSource.getRadiationLevel() + radiationSpread * (1 - targetChunkSource.getScrubbingFraction()));
                    }
                }
                chunkRadSource.resetSubchunk();
                targetChunkRadSource.resetSubchunk();
            }
        }

        for(int subchunk = 0; subchunk < 16; subchunk++) {
            chunkRadSource.setSubchunk(subchunk);

            chunkSource.setRadiationBuffer(0);
            if (chunkSource.isRadiationNegligible()) chunkSource.setRadiationLevel(0);
        }
        chunkRadSource.resetSubchunk();
    }
}
