package eaglemixins.mixin.sereneseasons;

import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import sereneseasons.config.FertilityConfig;
import sereneseasons.init.ModFertility;

/**
 * Adds metadata-aware fertility lookups to ModFertility.
 * Config entries may now optionally end in "@<meta>" (e.g. "minecraft:sapling@1")
 * to target a specific metadata variant of a shared-ID item. Entries without an
 * "@meta" suffix behave exactly as before (apply to all metadata variants).
 */
@Mixin(ModFertility.class)
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
     * Fully replaces the original method. Same signature, same call sites -
     * SeasonalCropGrowthHandler and ModFertility.init() are untouched.
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
            if (item instanceof IPlantable)
            {
                resolvedBlock = ((IPlantable) item).getPlant(null, null).getBlock();
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
     * New overload - NOT an @Overwrite, this is an additional method merged into
     * ModFertility by Mixin. SeasonalCropGrowthHandler's mixin calls this instead
     * of the original isCropFertile so metadata can be taken into account.
     * Falls back to the metadata-less key if no specific entry was found, so mixed
     * configs (some entries with @meta, some without) both resolve correctly.
     */
    public static boolean isCropFertileMeta(String plantName, Integer meta, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos)
    {
        String metaKey = (meta != null) ? (plantName + "@" + meta) : null;

        // Prefer the metadata-specific key if it was actually configured
        if (metaKey != null && allListedPlants.contains(metaKey))
        {
            return ModFertility.isCropFertile(metaKey, world, pos);
        }

        // Fall back to the bare (metadata-agnostic) key - preserves old behaviour
        return ModFertility.isCropFertile(plantName, world, pos);
    }

    @Overwrite
    @SideOnly(Side.CLIENT)
    public static void setupTooltips(ItemTooltipEvent event)
    {
        if (FertilityConfig.general_category.crop_tooltips && FertilityConfig.general_category.seasonal_crops)
        {
            String baseName = event.getItemStack().getItem().getRegistryName().toString();
            int meta = event.getItemStack().getItem().getHasSubtypes() ? event.getItemStack().getMetadata() : 0;
            String metaKey = baseName + "@" + meta;

            // Prefer a metadata-specific entry; fall back to the bare entry
            String lookupKey = seedSeasons.containsKey(metaKey) ? metaKey : baseName;

            if (seedSeasons.containsKey(lookupKey))
            {
                int mask = seedSeasons.get(lookupKey);

                event.getToolTip().add("Fertile Seasons:");

                if ((mask & 1) != 0 && (mask & 2) != 0 && (mask & 4) != 0 && (mask & 8) != 0)
                {
                    event.getToolTip().add(TextFormatting.LIGHT_PURPLE + " Year-Round");
                }
                else
                {
                    if ((mask & 1) != 0) event.getToolTip().add(TextFormatting.GREEN + " Spring");
                    if ((mask & 2) != 0) event.getToolTip().add(TextFormatting.YELLOW + " Summer");
                    if ((mask & 4) != 0) event.getToolTip().add(TextFormatting.GOLD + " Autumn");
                    if ((mask & 8) != 0) event.getToolTip().add(TextFormatting.AQUA + " Winter");
                }
            }
        }
    }
}
