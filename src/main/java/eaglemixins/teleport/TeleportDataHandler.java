package eaglemixins.teleport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eaglemixins.EagleMixins;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public final class TeleportDataHandler extends WorldSavedData {
    private static final String DATA_NAME = "eaglemixins_teleports";

    private final Map<Integer /*linkId*/, TeleportData> REGISTRY = new HashMap<>();

    // Used on very first creation, once per world
    private TeleportDataHandler() {
        super(DATA_NAME);

        // legacy read
        tryLoadFromJSON();
    }

    public TeleportData getTeleportData(int linkId) {
        // Lazy creation, directly taken from TeleporterConfig.parsedLinks.
        // We separate between the two maps since both can be overwritten by different means (exploration vs config change)
        return REGISTRY.computeIfAbsent(linkId, id ->
                ForgeConfigHandler.teleporter.getApproxTeleporterLinks().getOrDefault(id,
                    new TeleportData(new BlockPos(0, 80, 0), new BlockPos(0, 80, 0)) // This will hopefully never happen
                )
        );
    }

    public Map<Integer, TeleportData> getRegistry() {
        return REGISTRY;
    }

    /* ---------- WorldSavedData ---------- */

    // Used by MapStorage.getOrLoadData
    @SuppressWarnings("unused")
    public TeleportDataHandler(String name) {
        super(name);
    }

    // Get the class singleton holding the data
    public static TeleportDataHandler get(World world) {
        TeleportDataHandler data = (TeleportDataHandler) world.getMapStorage().getOrLoadData(TeleportDataHandler.class, DATA_NAME);

        if (data == null) { // First get after World creation -> register the data
            data = new TeleportDataHandler();
            world.getMapStorage().setData(DATA_NAME, data);
            data.markDirty();
        }

        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList(TeleportNBTKeys.DATA_TELEPORTERS, Constants.NBT.TAG_COMPOUND);
        for (NBTBase el : list) {
            NBTTagCompound tag = (NBTTagCompound) el;
            int id = tag.getInteger(TeleportNBTKeys.DATA_ID);
            try {

                BlockPos sender = NBTUtil.getPosFromTag(tag.getCompoundTag(TeleportNBTKeys.DATA_SENDER));
                BlockPos receiver = NBTUtil.getPosFromTag(tag.getCompoundTag(TeleportNBTKeys.DATA_RECEIVER));
                TeleportData data = new TeleportData(sender, receiver);

                if (tag.getBoolean(TeleportNBTKeys.DATA_SENDER_DISC)) data.setDiscoveredSenderPos(sender);
                if (tag.getBoolean(TeleportNBTKeys.DATA_RECEIVER_DISC)) data.setDiscoveredReceiverPos(receiver);

                REGISTRY.put(id, data);
            } catch (Exception e) {
                EagleMixins.LOGGER.error("Error reading teleport data {} from NBT, skipping", id, e);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        REGISTRY.forEach((id, data) -> {
            try {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger(TeleportNBTKeys.DATA_ID, id);
                tag.setTag(TeleportNBTKeys.DATA_SENDER, NBTUtil.createPosTag(data.sender));
                tag.setTag(TeleportNBTKeys.DATA_RECEIVER, NBTUtil.createPosTag(data.receiver));
                tag.setBoolean(TeleportNBTKeys.DATA_SENDER_DISC, data.isDiscovered(true));
                tag.setBoolean(TeleportNBTKeys.DATA_RECEIVER_DISC, data.isDiscovered(false));

                list.appendTag(tag);
            } catch (Exception e) {
                EagleMixins.LOGGER.error("Error writing teleport data {} to NBT, skipping", id, e);
            }
        });

        nbt.setTag(TeleportNBTKeys.DATA_TELEPORTERS, list);
        return nbt;
    }

    /* ---------- legacy read from JSON ---------- */

    @Deprecated
    private void tryLoadFromJSON() {
        WorldServer overworld = DimensionManager.getWorld(0);
        if (overworld == null) return;

        File configFile = new File(overworld.getSaveHandler().getWorldDirectory(), DATA_NAME + ".json");
        if (!configFile.exists()) return;

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();

            // Skip if already migrated
            if (root.has("deprecated") && root.get("deprecated").getAsBoolean()) {
                return;
            }

            REGISTRY.clear();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                if (entry.getKey().equals("deprecated")) continue; // skip metadata field

                try {
                    int linkId = Integer.parseInt(entry.getKey());
                    JsonObject link = entry.getValue().getAsJsonObject();

                    BlockPos sender = parseBlockPos(link.getAsJsonObject("sender"));
                    BlockPos receiver;
                    boolean receiverDiscovered;

                    if (link.has("receiver")) {
                        receiver = parseBlockPos(link.getAsJsonObject("receiver"));
                        receiverDiscovered = true;
                    } else if (link.has("tempReceiver")) { // obj always has tempReceiver, but we don't care if it also has (discovered)receiver
                        receiver = parseBlockPos(link.getAsJsonObject("tempReceiver"));
                        receiverDiscovered = false;
                    } else {
                        // This too should hopefully never happen
                        receiver = new BlockPos(0, 80, 0);
                        receiverDiscovered = false;
                    }

                    TeleportData data = new TeleportData(sender, receiver);
                    data.setDiscoveredSenderPos(sender); // we assume its discovered cause we don't know, and it will be overwritten anyway if it wasn't
                    if (receiverDiscovered) data.setDiscoveredReceiverPos(receiver);

                    REGISTRY.put(linkId, data);

                } catch (Exception e) {
                    EagleMixins.LOGGER.error("Failed to parse legacy JSON teleport data: {}", e.getMessage());
                }
            }

            EagleMixins.LOGGER.info("Migrated {} teleport links from legacy JSON", REGISTRY.size());

            // Mark as migrated
            root.addProperty("deprecated", true);
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(root.toString());
            }

        } catch (Exception e) {
            EagleMixins.LOGGER.error("Failed to load legacy teleport JSON: {}", e.getMessage());
        }
    }

    private BlockPos parseBlockPos(JsonObject obj) {
        // Try obfuscated format first (field_177962_a = x, field_177960_b = y, field_177961_c = z)
        if (obj.has("field_177962_a")) {
            return new BlockPos(
                obj.get("field_177962_a").getAsInt(),
                obj.get("field_177960_b").getAsInt(),
                obj.get("field_177961_c").getAsInt()
            );
        }
        // Deobf format {x, y, z}
        return new BlockPos(obj.get("x").getAsInt(), obj.get("y").getAsInt(), obj.get("z").getAsInt());
    }
}