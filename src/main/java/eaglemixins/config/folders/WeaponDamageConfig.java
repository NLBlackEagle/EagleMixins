package eaglemixins.config.folders;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;

import java.util.LinkedHashMap;
import java.util.Map;

public class WeaponDamageConfig {

    @Config.Comment({
            "Multiply the damage of a critical hit when it is dealt with one of the listed weapons.",
            "Only applies to player melee attacks and only when the hit actually crits.",
            "Apply Multiplier Late:",
            "- false: multiplier is applied on the crit multi directly (default x1.5), so damage = base * 1.5 * multiplier (+ enchant bonus).",
            "- true:  multiplier is applied to the fully resolved hit, so damage = (base * 1.5 + enchant bonus) * multiplier.",
            "Dmg Multiplier:decimal factor, e.g. 1.05 for +5%"
    })
    @Config.Name("Critical Hit Multipliers")
    public Map<ResourceLocation, CritEntry> critMultipliers = new LinkedHashMap<>();
    private void initCritMultis() {
        critMultipliers.put(new ResourceLocation("srparasites:weapon_axe"), new CritEntry(true, 1.0F));
        critMultipliers.put(new ResourceLocation("srparasites:weapon_axe_sentient"), new CritEntry(true, 1.0F));
    }
    public static class CritEntry {
        @Config.Name("Apply Multiplier Late")
        public boolean applyLate;
        @Config.Name("Dmg Multiplier")
        public float multiplier;

        public CritEntry(){} //needed for betterconfig
        public CritEntry(boolean applyLate, float multiplier) {
            this.applyLate = applyLate;
            this.multiplier = multiplier;
        }
    }

    @Config.Comment({
            "Multiply the damage of a hit with one of the listed weapons based on how far away the target is.",
            "Only applies to player melee attacks.",
            "Min Distance: Hits closer than min_distance always deal normal damage.",
            "Scales with Distance:",
            " - true: the bonus scales with the distance between attacker and target.",
            " - false: a flat bonus using \"Multiplier Base\" as the amount.",
            "Multiplier Base: The dmg multiplier applying at minimum distance (or anywhere beyond min distance if \"Scales With Distance\" is off)",
            "Multiplier per Block: Increases the base dmg multiplier by this amount for each additional block of distance beyond \"Min Distance\"."
    })
    @Config.Name("Range Multipliers")
    public Map<ResourceLocation, RangeEntry> rangeMultipliers = new LinkedHashMap<>();
    private void initRangeMultis(){
        rangeMultipliers.put(new ResourceLocation("srparasites:weapon_lance"), new RangeEntry(true, 4, 0.35F, 1.0F));
        rangeMultipliers.put(new ResourceLocation("srparasites:weapon_lance_sentient"), new RangeEntry(true, 4, 0.35F, 1.0F));
    }
    public static class RangeEntry {
        @Config.Name("Scales with Distance")
        public boolean scaleWithDistance;
        @Config.Name("Min Distance")
        public float minDistance;
        @Config.Name("Multiplier per block")
        public float perBlock;
        @Config.Name("Multiplier Base")
        public float baseMulti;

        public RangeEntry(){} //needed for betterconfig
        public RangeEntry(boolean scaleWithDistance, float minDistance, float perBlock, float baseMulti) {
            this.scaleWithDistance = scaleWithDistance;
            this.minDistance = minDistance;
            this.perBlock = perBlock;
            this.baseMulti = baseMulti;
        }

        public float calcDmgMultiplier(float distance) {
            if (distance < minDistance) return 1.0F;  // below the floor: normal damage
            if (!scaleWithDistance) return baseMulti;
            return baseMulti + perBlock * (distance - minDistance);
        }
    }

    @Config.Comment({
            "Final damage multiplier for Two-Handed I weapons when the off-hand is empty (i.e. the Two-Handed debuff is NOT active).",
            "  0.25 is the default, meaning +25% final damage (Damage * (1 + this)).",
            "  0.0 disables the buff for Two-Handed I weapons.",
            "Requires the \"Two-Handed Weapon Buff (SpartanWeaponry)\" mixin toggle."
    })
    @Config.Name("Two-Handed Buff Multiplier - Level 1 (SpartanWeaponry)")
    @Config.RangeDouble(min = 0.0D)
    public float spartanTwoHandedBuffLevel1 = 0.25F;

    @Config.Comment({
            "Final damage multiplier for Two-Handed II weapons when the off-hand is empty (i.e. the Two-Handed debuff is NOT active).",
            "  0.5 is the default, meaning +50% final damage (Damage * (1 + this)).",
            "  0.0 disables the buff for Two-Handed II weapons.",
            "Requires the \"Two-Handed Weapon Buff (SpartanWeaponry)\" mixin toggle."
    })
    @Config.Name("Two-Handed Buff Multiplier - Level 2 (SpartanWeaponry)")
    @Config.RangeDouble(min = 0.0D)
    public float spartanTwoHandedBuffLevel2 = 0.50F;

    public WeaponDamageConfig(){
        initCritMultis();
        initRangeMultis();
    }
}