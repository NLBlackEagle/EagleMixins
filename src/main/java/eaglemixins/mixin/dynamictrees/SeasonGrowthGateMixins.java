package eaglemixins.mixin.dynamictrees;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import sereneseasons.config.FertilityConfig;
import sereneseasons.init.ModFertility;

/**
 * Makes Dynamic Trees respect a hard seasonal gate, matching vanilla sapling
 * behavior in cropfertility.cfg (zero growth outside the listed season(s))
 * instead of DT's own soft growth-rate curve (which only ever slows growth,
 * never fully stops it).
 *
 * Species.grow() is DT's own original method (not a vanilla override), so it
 * has no SRG mapping - remap = false, same reasoning as the ModFertility
 * mixins. DT seeds use unique registry names per species (no shared
 * metadata), so this calls ModFertility.isCropFertile() directly - the exact
 * same check crops and vanilla saplings already use - no accessor needed.
 */
@Mixin(value = Species.class, remap = false)
public abstract class SeasonGrowthGateMixins
{
    @Inject(method = "grow", at = @At("HEAD"), cancellable = true, require = 1)
    private void eagleMixins$blockGrowthOutOfSeason(
            World world,
            BlockRooty rootyDirt,
            BlockPos rootPos,
            int soilLife,
            ITreePart treeBase,
            BlockPos treePos,
            Random random,
            boolean natural,
            CallbackInfoReturnable<Boolean> cir)
    {
        if (!FertilityConfig.general_category.seasonal_crops)
        {
            return; // master toggle off - defer to vanilla/DT behavior entirely
        }

        Species self = (Species) (Object) this;
        Seed seed = self.getSeed();

        if (seed == null || seed.getRegistryName() == null)
        {
            return; // can't resolve a registry name, don't touch growth
        }

        String registryName = seed.getRegistryName().toString();

        if (!ModFertility.isCropFertile(registryName, world, rootPos))
        {
            // Hard block: no growth this tick, matching vanilla out-of-season
            // behavior. Deliberately not respecting crops_break here - there's
            // no sensible single-block equivalent of "destroy the crop" for
            // an entire multi-block tree structure.
            cir.setReturnValue(false);
        }
    }
}
