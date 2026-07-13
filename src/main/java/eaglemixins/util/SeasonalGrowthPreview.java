package eaglemixins.util.dynamictrees;

/**
 * Replicates Dynamic Trees' SeasonGrowthCalculatorActive formula (verified
 * numerically to match the real mod's output). Deliberately standalone -
 * no DynamicTrees internals, no world/pos access, no reflection needed,
 * since the growth curve shape itself doesn't depend on live game state,
 * only which curve applies (based on the planted biome's rainfall) does.
 *
 * Internally still named temperate/tropical to match DT's own source, but
 * presented to players as "normal" / "wet" biomes - simpler, and honest
 * about what the rainfall>0.8 check actually measures (see SeasonManager).
 */
public class SeasonalGrowthPreview
{
    public static final int SPRING = 0;
    public static final int SUMMER = 1;
    public static final int AUTUMN = 2;
    public static final int WINTER = 3;

    private static float clippedSine(float seasonValue, float qPhase, float amplitude, float bias)
    {
        double v = Math.sin((seasonValue / 2.0f) * Math.PI + (Math.PI / 4.0) * qPhase) * amplitude + bias;
        return (float) Math.max(0.0, Math.min(1.0, v));
    }

    private static float temperateGrowth(float seasonValue)
    {
        return clippedSine(seasonValue, 7.0f, 0.8f, 1.0f);
    }

    private static float tropicalGrowth(float seasonValue)
    {
        return clippedSine(seasonValue, 2.0f, 0.31f, 0.9f);
    }

    /**
     * @param season 0=Spring, 1=Summer, 2=Autumn, 3=Winter
     */
    public static float temperateFactorAt(int season)
    {
        return temperateGrowth(season + 0.5f);
    }

    public static float tropicalFactorAt(int season)
    {
        return tropicalGrowth(season + 0.5f);
    }

    /**
     * 3-tier scale: collapsing this to a binary faster/slower split lost a
     * real distinction players care about (nearly-halted vs mildly reduced),
     * so kept as three tiers despite being slightly more to read.
     */
    public static String speedLabel(float factor)
    {
        if (factor >= 0.9f) return "Fast";
        if (factor >= 0.5f) return "Moderate";
        return "Slow";
    }
}
