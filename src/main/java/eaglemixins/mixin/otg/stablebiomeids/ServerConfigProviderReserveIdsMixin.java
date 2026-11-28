package eaglemixins.mixin.otg.stablebiomeids;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.pg85.otg.network.ServerConfigProvider;
import com.pg85.otg.worldsave.BiomeIdData;
import eaglemixins.util.OTGReservedBiomeIds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;

/**
 * Captures every saved biome id already written to biomeids.txt (all dimensions, including biomes that
 * no longer exist in the preset) so new biomes get an id on top of those instead of reusing one.
 */
@Mixin(ServerConfigProvider.class)
public abstract class ServerConfigProviderReserveIdsMixin {

    @ModifyExpressionValue(
            method = "indexSettings",
            at = @At(value = "INVOKE", target = "Lcom/pg85/otg/worldsave/BiomeIdData;loadBiomeIdData(Ljava/io/File;)Ljava/util/ArrayList;"),
            remap = false
    )
    private ArrayList<BiomeIdData> eaglemixins_reserveSavedBiomeIds(ArrayList<BiomeIdData> loaded) {
        // Every indexSettings call re-reads the file of the save being loaded, so this also stays correct
        // when switching between worlds in singleplayer.
        OTGReservedBiomeIds.reset();
        if (loaded != null) {
            for (BiomeIdData data : loaded) {
                OTGReservedBiomeIds.reserve(data.savedBiomeId);
            }
        }
        return loaded;
    }
}
