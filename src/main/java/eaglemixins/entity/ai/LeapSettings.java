package eaglemixins.entity.ai;

import eaglemixins.config.folders.LeapConfig;
import eaglemixins.util.RandomRange;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

/**
 * Resolved leap settings for one mob: a config {@link LeapConfig.Entry} with its range strings parsed,
 * optionally overridden by the mob's {@code ForgeData.EagleMixinsLeap} compound.
 */
public class LeapSettings {
    public double minRange;
    public double maxRange;
    public double minHeightAbove;
    public RandomRange distanceMultiplier;
    public RandomRange verticalPower;
    public RandomRange inaccuracy;
    public double maxHorizontalSpeed;
    public RandomRange cooldown;
    public float chance;
    public boolean lineOfSight;
    public boolean preventFallDamage;
    public boolean removeVanillaLeap;
    public int aiPriority;

    public static LeapSettings from(LeapConfig.Entry e) {
        LeapSettings s = new LeapSettings();
        s.minRange = e.minRange;
        s.maxRange = e.maxRange;
        s.minHeightAbove = e.minHeightAbove;
        s.distanceMultiplier = RandomRange.parse(e.distanceMultiplier, 1.0);
        s.verticalPower = RandomRange.parse(e.verticalPower, 0.5);
        s.inaccuracy = RandomRange.parse(e.inaccuracy, 0.0);
        s.maxHorizontalSpeed = e.maxHorizontalSpeed;
        s.cooldown = RandomRange.parse(e.cooldown, 60.0);
        s.chance = e.chance;
        s.lineOfSight = e.lineOfSight;
        s.preventFallDamage = e.preventFallDamage;
        s.removeVanillaLeap = e.removeVanillaLeap;
        s.aiPriority = e.aiPriority;
        return s;
    }

    public LeapSettings applyNbt(NBTTagCompound nbt) {
        minRange = getDouble(nbt, "minRange", minRange);
        maxRange = getDouble(nbt, "maxRange", maxRange);
        minHeightAbove = getDouble(nbt, "minHeight", minHeightAbove);
        distanceMultiplier = getRange(nbt, "distance", distanceMultiplier);
        verticalPower = getRange(nbt, "vertical", verticalPower);
        inaccuracy = getRange(nbt, "inaccuracy", inaccuracy);
        maxHorizontalSpeed = getDouble(nbt, "maxSpeed", maxHorizontalSpeed);
        cooldown = getRange(nbt, "cooldown", cooldown);
        chance = (float) getDouble(nbt, "chance", chance);
        lineOfSight = getBoolean(nbt, "lineOfSight", lineOfSight);
        preventFallDamage = getBoolean(nbt, "noFall", preventFallDamage);
        removeVanillaLeap = getBoolean(nbt, "removeVanilla", removeVanillaLeap);
        aiPriority = (int) getDouble(nbt, "priority", aiPriority);
        return this;
    }

    private static double getDouble(NBTTagCompound nbt, String key, double fallback) {
        return nbt.hasKey(key, Constants.NBT.TAG_ANY_NUMERIC) ? nbt.getDouble(key) : fallback;
    }

    private static boolean getBoolean(NBTTagCompound nbt, String key, boolean fallback) {
        return nbt.hasKey(key, Constants.NBT.TAG_ANY_NUMERIC) ? nbt.getBoolean(key) : fallback;
    }

    /** Accepts key:1.2, key:"0.9~1.4", or keyMin + keyMax (a missing side falls back to the current range). */
    private static RandomRange getRange(NBTTagCompound nbt, String key, RandomRange fallback) {
        if (nbt.hasKey(key, Constants.NBT.TAG_ANY_NUMERIC)) return RandomRange.of(nbt.getDouble(key));
        if (nbt.hasKey(key, Constants.NBT.TAG_STRING)) return RandomRange.parse(nbt.getString(key), fallback.min);
        boolean hasMin = nbt.hasKey(key + "Min", Constants.NBT.TAG_ANY_NUMERIC);
        boolean hasMax = nbt.hasKey(key + "Max", Constants.NBT.TAG_ANY_NUMERIC);
        if (!hasMin && !hasMax) return fallback;
        return RandomRange.of(
                hasMin ? nbt.getDouble(key + "Min") : fallback.min,
                hasMax ? nbt.getDouble(key + "Max") : fallback.max);
    }
}
