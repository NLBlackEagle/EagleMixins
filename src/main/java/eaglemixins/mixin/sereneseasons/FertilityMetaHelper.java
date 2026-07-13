package eaglemixins.util.sereneseasons;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import eaglemixins.mixin.sereneseasons.FertilityAccessor;
import sereneseasons.init.ModFertility;

/**
 * Ordinary utility class - NOT a mixin, so it isn't subject to the "no new
 * public methods merged into a target" restriction. Safe to call publicly
 * from any other class, including other mixins.
 */
public class FertilityMetaHelper
{
    /**
     * Prefers a metadata-specific fertility entry if one was actually configured
     * (e.g. "minecraft:sapling@1"); falls back to the metadata-agnostic entry
     * otherwise, so mixed configs (some entries with @meta, some without) both
     * resolve correctly.
     */
    public static boolean isCropFertileMeta(String plantName, Integer meta, World world, BlockPos pos)
    {
        String metaKey = (meta != null) ? (plantName + "@" + meta) : null;

        if (metaKey != null && FertilityAccessor.getAllListedPlants().contains(metaKey))
        {
            return ModFertility.isCropFertile(metaKey, world, pos);
        }

        return ModFertility.isCropFertile(plantName, world, pos);
    }
}
