package eaglemixins.mixin.sereneseasons;

import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import sereneseasons.init.ModFertility;

/**
 * Adds metadata-aware fertility lookups to ModFertility.
 * Config entries may now optionally end in "@<meta>" (e.g. "minecraft:sapling@1")
 * to target a specific metadata variant of a shared-ID item. Entries without an
 * "@meta" suffix behave exactly as before (apply to all metadata variants).
 *
 * The metadata-aware lookup entry point (isCropFertileMeta) does NOT live here -
 * Mixin rejects merging new non-private methods into a target class. It lives in
 * eaglemixins.util.sereneseasons.FertilityMetaHelper, an ordinary utility class,
 * which reaches this class's private allListedPlants field via FertilityAccessor.
 */
@Mixin(value = ModFertility.class, remap = false)
public abstract class FertilityMixins
{
    @Shadow private static HashSet<String> springPlants;
    @Shadow private static HashSet<String> summerPlants;
    @Shadow private static HashSet<String> autumnPlants;
    @Shadow private static HashSet<String> winterPlants;
    @Shadow private static HashSet<String> allListedPlants;
    @Shadow private static HashMap<String, Integer> seedSeasons;

    /**
     * Parses a config string into its base resource location and optional metadata.
     * Accepts "modid:path" or "modid:path@N". Returns null metadata if none specified.
     */
    private static Object[] parseSeedEntry(String seed)
    {
        int atIndex = seed.lastIndexOf('@');
        if (atIndex > 0)
        {
            String metaStr = seed.substring(atIndex + 1);
            String base = seed.substring(0, atIndex);
            try
            {
                int meta = Integer.parseInt(metaStr);
                return new Object[] { base, meta };
            }
            catch (NumberFormatException ignored)
            {
                // Not a valid integer after '@' - treat whole string as the resource location
            }
        }
        return new Object[] { seed, null };
    }

    /**
     * @author NLBlackEagle
     * @reason Seasons does not support metadata, now it does.
     */
    @Overwrite
    private static void initSeasonCrops(String[] seeds, HashSet<String> cropSet, int bitmask)
    {
        for (String rawSeed : seeds)
        {
            Object[] parsed = parseSeedEntry(rawSeed);
            String resourceStr = (String) parsed[0];
            Integer meta = (Integer) parsed[1];

            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(resourceStr));

            Block resolvedBlock = null;

            if (item instanceof ItemBlock)
            {
                // Safe, direct lookup - no null world/pos risk at all. Covers most
                // "place directly" plant items: saplings, herb blocks, mushrooms, etc.
                resolvedBlock = ((ItemBlock) item).getBlock();
            }
            else if (item instanceof IPlantable)
            {
                try
                {
                    // Needed for genuine seed items (e.g. ItemSeeds like wheat/carrot)
                    // that aren't themselves an ItemBlock and only exist to be planted
                    // into a separate crop block.
                    resolvedBlock = ((IPlantable) item).getPlant(null, null).getBlock();
                }
                catch (Exception e)
                {
                    // Some mods' IPlantable#getPlant implementations don't handle a
                    // null world/pos gracefully and throw instead of returning a
                    // sensible default. Skip this entry rather than crashing.
                    resolvedBlock = null;
                }
            }
            else
            {
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(resourceStr));
                if (block != null && block != Blocks.AIR)
                {
                    resolvedBlock = block;
                }
            }

            if (resolvedBlock == null)
            {
                continue; // silently skip, same behaviour as vanilla when unresolved
            }

            String plantName = resolvedBlock.getRegistryName().toString();
            String key = (meta != null) ? (plantName + "@" + meta) : plantName;

            cropSet.add(key);

            if (bitmask == 0)
            {
                continue;
            }

            allListedPlants.add(key);

            String seedKey = (meta != null) ? (resourceStr + "@" + meta) : resourceStr;
            if (seedSeasons.containsKey(seedKey))
            {
                int seasons = seedSeasons.get(seedKey);
                seedSeasons.put(seedKey, seasons | bitmask);
            }
            else
            {
                seedSeasons.put(seedKey, bitmask);
            }
        }
    }

    /**
     * Non-destructive: only swaps the lookup key to a metadata-aware one if a
     * matching entry exists. Deliberately NOT an @Overwrite - other mods (e.g.
     * localization mods) may inject into this method's original tooltip-adding
     * calls, and an @Overwrite would destroy those anchor points, breaking them.
     */
    @ModifyVariable(method = "setupTooltips", at = @At("STORE"), ordinal = 0)
    private static String eagleMixins$useMetaKeyIfPresent(String name, ItemTooltipEvent event)
    {
        int meta = event.getItemStack().getItem().getHasSubtypes() ? event.getItemStack().getMetadata() : 0;
        String metaKey = name + "@" + meta;

        return seedSeasons.containsKey(metaKey) ? metaKey : name;
    }
}
