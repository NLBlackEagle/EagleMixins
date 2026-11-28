package eaglemixins.util;

import java.util.BitSet;

/**
 * Saved (chunk/Forge registry) biome ids that are recorded in the world's OTG biomeids.txt.
 * These ids belong to a biome in the save (possibly of a dimension that isn't loaded right now,
 * or of a biome that was removed from the preset), so a brand-new biome must never be handed one of them.
 */
public final class OTGReservedBiomeIds {
    private static final BitSet RESERVED = new BitSet(256);

    private OTGReservedBiomeIds() {}

    public static synchronized void reset() {
        RESERVED.clear();
    }

    public static synchronized void reserve(int savedId) {
        if (savedId >= 0) RESERVED.set(savedId);
    }

    public static synchronized boolean isReserved(int savedId) {
        return savedId >= 0 && RESERVED.get(savedId);
    }
}
