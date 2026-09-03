package eaglemixins.config.folders;

import eaglemixins.EagleMixins;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Config;

import java.util.HashMap;
import java.util.Map;

public class WeaponDamageConfig {

    @Config.Comment({
            "Multiply the damage of a critical hit (the vanilla jump-crit) when it is dealt with one of the listed weapons.",
            "Only applies to player melee attacks and only when the hit actually crits.",
            "Syntax: modid:name@meta, before|after, multiplier",
            "  modid:name@meta - the weapon. \"@meta\" is optional; leave it off (or use @*) to match any metadata.",
            "  before|after    - \"before\": multiplier is folded into the crit step, so damage = base * multiplier * 1.5 (+ enchant bonus, unscaled).",
            "                    \"after\":  multiplier is applied to the fully resolved hit, so damage = (base * 1.5 + enchant bonus) * multiplier.",
            "  multiplier      - decimal factor, e.g. 1.05",
            "Example: srparasites:weapon_axe, after, 1.05",
            "Example: srparasites:weapon_axe_sentient, after, 1.05"
    })
    @Config.Name("Critical Hit Multipliers")
    public String[] critMultipliers = {
            "srparasites:weapon_axe, after, 1.05",
            "srparasites:weapon_axe_sentient, after, 1.05"
    };

    @Config.Comment({
            "Multiply the damage of a hit with one of the listed weapons based on how far away the target is.",
            "Only applies to player melee attacks. Meant for long-reach weapons like a lance.",
            "Syntax: modid:name@meta, dynamic|set_amount, min_distance, multiplier, base",
            "  modid:name@meta    - the weapon. \"@meta\" is optional; leave it off (or use @*) to match any metadata.",
            "  dynamic|set_amount - hits closer than min_distance always deal normal damage. At or beyond min_distance:",
            "                       \"dynamic\":    the bonus scales with the exact distance (the weapon's reach caps the top).",
            "                       \"set_amount\": a flat bonus using min_distance as the amount (no scaling with distance).",
            "  min_distance       - blocks; the activation floor, and the flat amount used by set_amount.",
            "  multiplier, base   - final multiplier = (multiplier - base) * amount + base. base is normally 1.0.",
            "                       e.g. multiplier 1.35, base 1.0, amount 4 -> (0.35 * 4) + 1.0 = 2.4",
            "Example: srparasites:weapon_lance, dynamic, 4, 1.35, 1.0",
            "Example: srparasites:weapon_lance_sentient, set_amount, 4, 1.35, 1.0"
    })
    @Config.Name("Range Multipliers")
    public String[] rangeMultipliers = {
            "srparasites:weapon_lance, dynamic, 4, 1.35, 1.0",
            "srparasites:weapon_lance_sentient, dynamic, 4, 1.35, 1.0"
    };

    @Config.Comment({
            "Replaces Better Survival's nunchaku combo damage multiplier with a flat value.",
            "Better Survival normally multiplies nunchaku hits by (1.0 + combo power), ramping up to 2.0x as the combo builds.",
            "This overrides that with a fixed multiplier; combo power no longer affects damage.",
            "1.0 = nunchaku hits deal normal weapon damage (combo damage bonus disabled).",
            "Requires the \"Nunchaku Combo Multiplier Override (BetterSurvival)\" mixin toggle."
    })
    @Config.Name("Nunchaku Combo Damage Multiplier (BetterSurvival)")
    @Config.RangeDouble(min = 0.0D)
    public float nunchakuComboMultiplier = 1.0F;

    @Config.Comment({
            "Starting Modifier for Nunchaku (Damage * (1 + this))",
            "\t0.0 is the default, meaning no bonus damage or penalty and matches the damage amount listed on the tooltip.",
            "\tA negative amount, such as -0.5 (-50% damage), can be provided.",
            "\tA negative amount can be used to have Nunchaku start dealing damage less than the listed tooltip amount.",
            "Requires the \"Nunchaku Combo Mechanic Configuration (BetterSurvival)\" mixin toggle."
    })
    @Config.Name("Nunchaku Combo Starting Amount (BetterSurvival)")
    @Config.RangeDouble(min = -1.0D)
    public float nunchakuComboStartAmount = 0.0F;

    @Config.Comment({
            "Maximum Modifier for Nunchaku (Damage * (1 + this))",
            "\t1.0 is the default, meaning up to +100% damage dealt",
            "\t0.0 means up to +0% and the mechanic would be disabled if the starting amount was +0% also.",
            "\t0.0 would be exactly the damage amount listed on the tooltip.",
            "Requires the \"Nunchaku Combo Mechanic Configuration (BetterSurvival)\" mixin toggle."
    })
    @Config.Name("Nunchaku Combo Capped Amount (BetterSurvival)")
    @Config.RangeDouble(min = -1.0D)
    public float nunchakuComboCapAmount = 1.0F;

    @Config.Comment({
            "The bonus damage amount that is gained per hit AT MINIMUM.",
            "\t0.0 means no bonus is gained",
            "\t0.1 is the default, meaning +10% combo damage is gained until it reaches the capped bonus",
            "Requires the \"Nunchaku Combo Mechanic Configuration (BetterSurvival)\" mixin toggle."
    })
    @Config.Name("Nunchaku Combo Base Increase Rate (BetterSurvival)")
    @Config.RangeDouble(min = 0.0D)
    public float nunchakuComboBaseRate = 0.1F;

    @Config.Comment({
            "The bonus damage amount that is gained per hit from the Combo Enchantment.",
            "\t0.0 means no bonus is gained",
            "\t0.05 is the default, meaning +5% combo damage per level of combo, up to 15% with combo 3, is gained.",
            "Requires the \"Nunchaku Combo Mechanic Configuration (BetterSurvival)\" mixin toggle."
    })
    @Config.Name("Nunchaku Combo Enchantment Increase Rate (BetterSurvival)")
    @Config.RangeDouble(min = 0.0D)
    public float nunchakuComboEnchantmentRate = 0.05F;

    public static final class CritEntry {
        public final boolean afterCrit;
        public final float multiplier;

        CritEntry(boolean afterCrit, float multiplier) {
            this.afterCrit = afterCrit;
            this.multiplier = multiplier;
        }
    }

    public static final class RangeEntry {
        public final boolean dynamic;
        public final float minDistance;
        public final float multiplier;
        public final float base;

        RangeEntry(boolean dynamic, float minDistance, float multiplier, float base) {
            this.dynamic = dynamic;
            this.minDistance = minDistance;
            this.multiplier = multiplier;
            this.base = base;
        }

        /** Damage multiplier for a hit at {@code distance} blocks; 1.0 means no change. */
        public float compute(float distance) {
            if (distance < minDistance) return 1.0F;          // below the floor: normal damage
            float amount = dynamic ? distance : minDistance;  // dynamic scales; set_amount is flat
            return (multiplier - base) * amount + base;
        }
    }

    //Map<Item, Map<Metadata, Entry>>, metadata -1 means "any metadata"
    private final Map<Item, Map<Integer, CritEntry>> critMap = new HashMap<>();
    private final Map<Item, Map<Integer, RangeEntry>> rangeMap = new HashMap<>();
    private boolean critParsed = false;
    private boolean rangeParsed = false;

    public CritEntry getCritEntry(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        if (!critParsed) parseCrit();
        Map<Integer, CritEntry> byMeta = critMap.get(stack.getItem());
        if (byMeta == null) return null;
        CritEntry exact = byMeta.get(stack.getMetadata());
        return exact != null ? exact : byMeta.get(-1);
    }

    public RangeEntry getRangeEntry(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        if (!rangeParsed) parseRange();
        Map<Integer, RangeEntry> byMeta = rangeMap.get(stack.getItem());
        if (byMeta == null) return null;
        RangeEntry exact = byMeta.get(stack.getMetadata());
        return exact != null ? exact : byMeta.get(-1);
    }

    private void parseCrit() {
        critParsed = true;
        for (String raw : critMultipliers) {
            String[] split = raw.split(",");
            if (split.length < 3) {
                EagleMixins.LOGGER.error("Failed parsing critical hit multiplier ({}) - expected 3 comma-separated fields", raw);
                continue;
            }
            try {
                Item item = parseItem(split[0]);
                int meta = parseMeta(split[0]);
                if (item == null) {
                    EagleMixins.LOGGER.error("Failed parsing critical hit multiplier ({}) - unknown item", raw);
                    continue;
                }
                boolean afterCrit = parseAfterCrit(split[1].trim());
                float multiplier = Float.parseFloat(split[2].trim());
                critMap.computeIfAbsent(item, k -> new HashMap<>()).put(meta, new CritEntry(afterCrit, multiplier));
            } catch (Exception e) {
                EagleMixins.LOGGER.error("Failed parsing critical hit multiplier ({})", raw);
            }
        }
    }

    private void parseRange() {
        rangeParsed = true;
        for (String raw : rangeMultipliers) {
            String[] split = raw.split(",");
            if (split.length < 5) {
                EagleMixins.LOGGER.error("Failed parsing range multiplier ({}) - expected 5 comma-separated fields", raw);
                continue;
            }
            try {
                Item item = parseItem(split[0]);
                int meta = parseMeta(split[0]);
                if (item == null) {
                    EagleMixins.LOGGER.error("Failed parsing range multiplier ({}) - unknown item", raw);
                    continue;
                }
                boolean dynamic = parseDynamic(split[1].trim());
                float minDistance = Float.parseFloat(split[2].trim());
                float multiplier = Float.parseFloat(split[3].trim());
                float base = Float.parseFloat(split[4].trim());
                rangeMap.computeIfAbsent(item, k -> new HashMap<>()).put(meta, new RangeEntry(dynamic, minDistance, multiplier, base));
            } catch (Exception e) {
                EagleMixins.LOGGER.error("Failed parsing range multiplier ({})", raw);
            }
        }
    }

    private static Item parseItem(String itemSpec) {
        String id = itemSpec.trim();
        int at = id.indexOf('@');
        if (at >= 0) id = id.substring(0, at).trim();
        return Item.getByNameOrId(id);
    }

    private static int parseMeta(String itemSpec) {
        int at = itemSpec.indexOf('@');
        if (at < 0) return -1;
        String meta = itemSpec.substring(at + 1).trim();
        if (meta.isEmpty() || meta.equals("*")) return -1;
        return Integer.parseInt(meta);
    }

    private static boolean parseAfterCrit(String mode) {
        if (mode.equalsIgnoreCase("after")) return true;
        if (mode.equalsIgnoreCase("before")) return false;
        throw new IllegalArgumentException("expected 'before' or 'after', got '" + mode + "'");
    }

    private static boolean parseDynamic(String mode) {
        if (mode.equalsIgnoreCase("dynamic")) return true;
        if (mode.equalsIgnoreCase("set_amount")) return false;
        throw new IllegalArgumentException("expected 'dynamic' or 'set_amount', got '" + mode + "'");
    }

    public void reset() {
        critMap.clear();
        rangeMap.clear();
        critParsed = false;
        rangeParsed = false;
    }
}