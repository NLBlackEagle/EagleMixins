package eaglemixins.mixin.otg.stablebiomeids;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.pg85.otg.forge.biomes.ForgeBiomeRegistryManager;
import eaglemixins.EagleMixins;
import eaglemixins.util.OTGReservedBiomeIds;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.BitSet;

/**
 * OTG picks a saved id for a new biome with {@code availabilityMap.nextClearBit(0)}: the lowest id that is not
 * registered right now. Ids of biomes in other (not yet loaded / unloaded) dimensions or of removed biomes are
 * "free" at that moment, so the new biome steals them and old chunks storing that id turn into the new biome.
 * Here the search also skips every id recorded in biomeids.txt.
 */
@Mixin(ForgeBiomeRegistryManager.class)
public abstract class ForgeBiomeRegistryManagerMixin {

    @WrapOperation(
            method = "registerForgeBiomeWithId(ILnet/minecraft/util/ResourceLocation;Lnet/minecraft/world/biome/Biome;)I",
            at = @At(value = "INVOKE", target = "Ljava/util/BitSet;nextClearBit(I)I"),
            remap = false
    )
    private int eaglemixins_skipReservedSavedIds(BitSet availability, int fromIndex, Operation<Integer> original, int requestedId, ResourceLocation registryKey) {
        int vanillaPick = original.call(availability, fromIndex);
        int limit = ForgeBiomeRegistryManager.JEID ? Integer.MAX_VALUE : 256;

        int id = vanillaPick;
        while (id >= 0 && id < limit && OTGReservedBiomeIds.isReserved(id)) {
            id = original.call(availability, id + 1);
        }

        if (id < 0 || id >= limit) {
            EagleMixins.LOGGER.warn("No unreserved biome id left for {}, falling back to OTG's pick {} (it may overlap an id stored in biomeids.txt)", registryKey, vanillaPick);
            return vanillaPick;
        }
        if (id != vanillaPick) {
            EagleMixins.LOGGER.info("Assigned saved biome id {} to new biome {} (skipped ids reserved in biomeids.txt)", id, registryKey);
        }
        return id;
    }
}
