package eaglemixins.teleport;

public class TeleportNBTKeys {
    // Used for SpawnListener entity (marking the exact positions of the teleporters)
    public static final String ENTITY_LINKID = "linkId";
    public static final String ENTITY_ISSENDER = "isSender";

    // Used for player when teleporting to not yet generated teleporter structures
    public static final String PLAYER_CURR_LINKID = "em$linkId";
    public static final String PLAYER_CURR_DIRECTION = "em$direction"; // boolean, true: S -> R, false: R -> S

    // Player NBT, only used when teleporting to Underneath
    public static final String NOT_FIRST_TIME = "glitchDone"; // <-- this stays present in the data, the rest is added+removed dynamically
    public static final String RETURN_TIME = "em$glitchEndTime";
    public static final String RETURN_DIM = "em$glitchOriginDim";
    public static final String RETURN_X = "em$glitchReturnX";
    public static final String RETURN_Y = "em$glitchReturnY";
    public static final String RETURN_Z = "em$glitchReturnZ";

    // WorldSavedData eaglemixins_teleports.dat
    public static final String DATA_TELEPORTERS = "teleporters";
    public static final String DATA_ID = "id";
    public static final String DATA_SENDER = "sender";
    public static final String DATA_RECEIVER = "receiver";
    public static final String DATA_SENDER_DISC = "senderIsDiscovered";
    public static final String DATA_RECEIVER_DISC = "receiverIsDiscovered";
}
