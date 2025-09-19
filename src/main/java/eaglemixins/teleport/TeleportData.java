package eaglemixins.teleport;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds exact (discovered) positions plus static fallback ("approx") coords for each linkId.
 * The approx coords live as static data here so we don't need a separate class/file.
 */
public class TeleportData {
    /* Persisted fields */
    public BlockPos sender;        // exact sender pad (may be null before discovered)
    public BlockPos receiver;      // exact receiver pad (may be null before discovered)
    public BlockPos tempReceiver;  // temporary receiver fallback (often approxReceiver)

    /* ---- Static fallback table (per linkId) ---- */

    public static final int[] ALL_IDS = {0, 1, 2, 3, 4, 5, 6};

    public static final class Pair {
        public final BlockPos senderApprox;
        public final BlockPos receiverApprox;
        public Pair(BlockPos s, BlockPos r) { this.senderApprox = s; this.receiverApprox = r; }
    }

    private static final Map<Integer, Pair> DEFAULTS = new HashMap<>();

    static {
        // linkId -> (senderApprox, receiverApprox)
        DEFAULTS.put(0, new Pair(new BlockPos(-950, 80, -2228),  new BlockPos(-8247, 80, -12929))); // Frozen Greens
        DEFAULTS.put(1, new Pair(new BlockPos(-2355, 80, -642),  new BlockPos(-15878, 80, -4301))); // The Highlands
        DEFAULTS.put(2, new Pair(new BlockPos(-1987, 80, 1453),  new BlockPos(-13520, 80, 7636)));  // Valley of Sulfur
        DEFAULTS.put(3, new Pair(new BlockPos(-116, 80, 2455),   new BlockPos(-2607, 80, 16323)));  // Southern Green
        DEFAULTS.put(4, new Pair(new BlockPos(2407, 80, -401),   new BlockPos(13476, 80, -8398)));  // Sea of Decay
        DEFAULTS.put(5, new Pair(new BlockPos(1171, 80, -2126),  new BlockPos(7070, 80, -15844)));  // Green Desert
        DEFAULTS.put(6, new Pair(new BlockPos(1831, 80, 1635),   new BlockPos(5502, 80, 13754)));   // Permafrost
    }

    public static Pair defaults(int linkId) {
        return DEFAULTS.getOrDefault(linkId, new Pair(new BlockPos(0, 80, 0), new BlockPos(0, 80, 0)));
    }

    public static BlockPos senderApprox(int linkId)   { return defaults(linkId).senderApprox; }
    public static BlockPos receiverApprox(int linkId) { return defaults(linkId).receiverApprox; }
}
