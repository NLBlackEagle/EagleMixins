package eaglemixins.entity.ai;

import eaglemixins.EagleMixins;
import eaglemixins.util.RandomRange;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;

/**
 * Leap settings for one mob. Built from "key=value" pairs (config lines) or the ForgeData.EagleMixinsLeap compound,
 * both using the same keys. Range keys take "1.2", "0.9~1.4", or keyMin / keyMax separately.
 */
public class LeapSettings {
    public double minRange = 4;
    public double maxRange = 10;
    public double minHeightAbove = -64;
    public RandomRange distanceMultiplier = RandomRange.of(1);
    public RandomRange leapDistance = RandomRange.of(0);
    public boolean arc = true;
    public RandomRange arcHeight = RandomRange.of(0.25, 0.3);
    public RandomRange arcSpeed = RandomRange.of(0.5, 0.6);
    public RandomRange verticalPower = RandomRange.of(0.5);
    public RandomRange inaccuracy = RandomRange.of(0);
    public double maxHorizontalSpeed = 2;
    public RandomRange cooldown = RandomRange.of(60);
    public float chance = 0.3F;
    public boolean lineOfSight = true;
    public boolean preventFallDamage = true;
    public boolean removeVanillaLeap = false;
    public int aiPriority = 3;

    public LeapSettings copy() {
        LeapSettings s = new LeapSettings();
        s.minRange = minRange;
        s.maxRange = maxRange;
        s.minHeightAbove = minHeightAbove;
        s.distanceMultiplier = distanceMultiplier;
        s.leapDistance = leapDistance;
        s.arc = arc;
        s.arcHeight = arcHeight;
        s.arcSpeed = arcSpeed;
        s.verticalPower = verticalPower;
        s.inaccuracy = inaccuracy;
        s.maxHorizontalSpeed = maxHorizontalSpeed;
        s.cooldown = cooldown;
        s.chance = chance;
        s.lineOfSight = lineOfSight;
        s.preventFallDamage = preventFallDamage;
        s.removeVanillaLeap = removeVanillaLeap;
        s.aiPriority = aiPriority;
        return s;
    }

    /** Applies "key=value, key=value, ...". Bad pairs are logged with {@code context} and skipped. */
    public LeapSettings apply(String pairs, String context) {
        for (String pair : pairs.split(",")) {
            if (pair.trim().isEmpty()) continue;
            String[] kv = pair.split("=", 2);
            if (kv.length != 2 || !set(kv[0].trim(), kv[1].trim()))
                EagleMixins.LOGGER.warn("Leaping Mobs: invalid setting \"{}\" in \"{}\"", pair.trim(), context);
        }
        return this;
    }

    /** Applies the ForgeData.EagleMixinsLeap compound. Numbers and strings both work as values. */
    public LeapSettings apply(NBTTagCompound nbt) {
        for (String key : nbt.getKeySet()) {
            NBTBase tag = nbt.getTag(key);
            String value;
            if (tag instanceof NBTPrimitive) value = String.valueOf(((NBTPrimitive) tag).getDouble());
            else if (tag instanceof NBTTagString) value = ((NBTTagString) tag).getString();
            else value = null;
            if (value == null || !set(key, value))
                EagleMixins.LOGGER.warn("Leaping Mobs: invalid NBT setting {}:{}", key, tag);
        }
        return this;
    }

    /** Sets one setting, returns false if the key is unknown or the value is invalid. */
    public boolean set(String key, String value) {
        try {
            switch (key) {
                case "minRange": minRange = Double.parseDouble(value); return true;
                case "maxRange": maxRange = Double.parseDouble(value); return true;
                case "minHeight": minHeightAbove = Double.parseDouble(value); return true;
                case "maxSpeed": maxHorizontalSpeed = Double.parseDouble(value); return true;
                case "chance": chance = (float) Double.parseDouble(value); return true;
                case "priority": aiPriority = (int) Double.parseDouble(value); return true;
                case "lineOfSight": lineOfSight = parseBoolean(value); return true;
                case "noFall": preventFallDamage = parseBoolean(value); return true;
                case "removeVanilla": removeVanillaLeap = parseBoolean(value); return true;
                case "arc": arc = parseBoolean(value); return true;
            }
            if (getRange(key) != null) {
                RandomRange range = RandomRange.parse(value);
                return range != null && setRange(key, range);
            }
            if (key.endsWith("Min") || key.endsWith("Max")) {
                String rangeKey = key.substring(0, key.length() - 3);
                RandomRange current = getRange(rangeKey);
                if (current == null) return false;
                double v = Double.parseDouble(value);
                return setRange(rangeKey, key.endsWith("Min") ? RandomRange.of(v, current.max) : RandomRange.of(current.min, v));
            }
        } catch (IllegalArgumentException ignored) {} //includes NumberFormatException
        return false;
    }

    private RandomRange getRange(String key) {
        switch (key) {
            case "distance": return distanceMultiplier;
            case "leapDistance": return leapDistance;
            case "arcHeight": return arcHeight;
            case "arcSpeed": return arcSpeed;
            case "vertical": return verticalPower;
            case "inaccuracy": return inaccuracy;
            case "cooldown": return cooldown;
            default: return null;
        }
    }

    private boolean setRange(String key, RandomRange range) {
        switch (key) {
            case "distance": distanceMultiplier = range; return true;
            case "leapDistance": leapDistance = range; return true;
            case "arcHeight": arcHeight = range; return true;
            case "arcSpeed": arcSpeed = range; return true;
            case "vertical": verticalPower = range; return true;
            case "inaccuracy": inaccuracy = range; return true;
            case "cooldown": cooldown = range; return true;
            default: return false;
        }
    }

    private static boolean parseBoolean(String value) {
        if (value.equalsIgnoreCase("true") || value.equals("1") || value.equals("1.0")) return true;
        if (value.equalsIgnoreCase("false") || value.equals("0") || value.equals("0.0")) return false;
        throw new IllegalArgumentException(value);
    }
}
