package eaglemixins.util;

import java.util.Random;

/**
 * Implemented on OTG's ForgeWorld: the stronghold of a chunk is remembered during OTG's default structure step
 * and only placed once OTG finished populating that chunk.
 */
public interface IDeferredStronghold {
    void eaglemixins$placeDeferredStronghold(Random rand);
}
