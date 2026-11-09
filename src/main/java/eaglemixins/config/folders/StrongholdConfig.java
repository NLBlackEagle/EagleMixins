package eaglemixins.config.folders;

import net.minecraftforge.common.config.Config;

public class StrongholdConfig {

    @Config.Comment({
            "Pushes every stronghold ring this many blocks further away from spawn, keeping the normal ring layout beyond it.",
            "With vanilla ring settings the first ring then sits at about this + 1,400 to 2,700 blocks.",
            "Only affects newly generated worlds/dimensions: existing strongholds stay where they are, but eyes of ender use the new positions."
    })
    @Config.Name("Ring Offset (blocks)")
    @Config.RangeInt(min = 0)
    public int ringOffset = 10000;

    @Config.Comment({
            "Strongholds in the first ring. Each next ring gets more (vanilla: 3, 6, 10, 15, 21, ...).",
            "Since the rings are pushed outward they are a lot bigger, so they need more strongholds to not be far apart.",
            "0 = keep the world's own value (vanilla 3, or StrongholdSpread in the OTG WorldConfig)."
    })
    @Config.Name("Strongholds In First Ring")
    @Config.RangeInt(min = 0)
    public int spread = 12;

    @Config.Comment({
            "Total amount of strongholds in the world. Once they run out, no further rings are made.",
            "With the defaults (offset 10000, 12 in the first ring) 256 fills rings from ~12,000 to ~27,000 blocks.",
            "Higher values reach further out but make the first stronghold lookup (eye of ender, worldgen) slower.",
            "0 = keep the world's own value (vanilla 128, or StrongholdCount in the OTG WorldConfig)."
    })
    @Config.Name("Stronghold Count")
    @Config.RangeInt(min = 0)
    public int count = 256;

    @Config.Comment({
            "Eye of Ender trail color: at this distance (blocks, horizontal) or further from the stronghold the trail has the \"Far\" color,",
            "at half of it the \"Middle\" color and right on top of the stronghold the \"Near\" color, with a gradient in between.",
            "Requires the \"Eye Of Ender Distance Trail (Vanilla)\" mixin toggle."
    })
    @Config.Name("Eye Trail Far Distance (blocks)")
    @Config.RangeInt(min = 1)
    public int eyeTrailFarDistance = 10000;

    @Config.Comment("Eye of Ender trail color (hex) when far away from the stronghold. Particles still get the vanilla random brightness.")
    @Config.Name("Eye Trail Far Color")
    public String eyeTrailFarColor = "#804DFF";

    @Config.Comment("Eye of Ender trail color (hex) at half the far distance. #E64DFF is the vanilla purple.")
    @Config.Name("Eye Trail Middle Color")
    public String eyeTrailMidColor = "#E64DFF";

    @Config.Comment("Eye of Ender trail color (hex) when right on top of the stronghold.")
    @Config.Name("Eye Trail Near Color")
    public String eyeTrailNearColor = "#FF4073";

    private float[][] parsedEyeColors = null;

    public void reset() {
        parsedEyeColors = null;
    }

    /**
     * RGB (0-1) of the eye trail at the given horizontal distance to the stronghold.
     */
    public float[] getEyeTrailColor(float distance) {
        if (parsedEyeColors == null) {
            parsedEyeColors = new float[][]{
                    parseColor(eyeTrailNearColor, 0xFF4073),
                    parseColor(eyeTrailMidColor, 0xE64DFF),
                    parseColor(eyeTrailFarColor, 0x804DFF)};
        }
        float t = Math.max(0.0F, Math.min(1.0F, distance / Math.max(1, eyeTrailFarDistance)));
        float[] from = t < 0.5F ? parsedEyeColors[0] : parsedEyeColors[1];
        float[] to = t < 0.5F ? parsedEyeColors[1] : parsedEyeColors[2];
        float f = t < 0.5F ? t * 2.0F : (t - 0.5F) * 2.0F;
        return new float[]{
                from[0] + (to[0] - from[0]) * f,
                from[1] + (to[1] - from[1]) * f,
                from[2] + (to[2] - from[2]) * f};
    }

    private static float[] parseColor(String hex, int fallback) {
        int rgb = fallback;
        try {
            rgb = Integer.parseInt(hex.trim().replace("#", ""), 16);
        } catch (NumberFormatException ignored) {
        }
        return new float[]{((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F};
    }
}
