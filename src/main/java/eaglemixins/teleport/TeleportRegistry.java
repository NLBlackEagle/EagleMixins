package eaglemixins.teleport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class TeleportRegistry {
    private static final Logger LOGGER = LogManager.getLogger("EagleMixins");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // linkId -> data
    private static final Map<Integer, TeleportData> REGISTRY = new HashMap<>();
    private static File configFile;
    private static boolean initialized = false;

    private TeleportRegistry() {}

    public static synchronized void ensureInit() {
        if (initialized) return;
        reloadConfigFile();
        loadFromDisk();
        seedMissingFromDefaults();
        saveToDisk(); // in case we added seeds
        initialized = true;
    }

    public static synchronized TeleportData getOrCreate(int linkId) {
        ensureInit();
        return REGISTRY.computeIfAbsent(linkId, id -> {
            TeleportData d = new TeleportData();
            // Keep original behavior: use approx sender to enable near checks immediately;
            // and use approx receiver as tempReceiver until a real receiver is discovered.
            d.sender = TeleportData.senderApprox(id);
            d.tempReceiver = TeleportData.receiverApprox(id);
            return d;
        });
    }

    public static synchronized TeleportData get(int linkId) {
        ensureInit();
        return REGISTRY.get(linkId);
    }

    public static synchronized void put(int linkId, TeleportData data) {
        ensureInit();
        REGISTRY.put(linkId, data);
        saveToDisk();
    }

    public static synchronized void updateSender(int linkId, BlockPos sender) {
        TeleportData d = getOrCreate(linkId);
        d.sender = sender;
        put(linkId, d);
    }

    public static synchronized void updateReceiver(int linkId, BlockPos receiver) {
        TeleportData d = getOrCreate(linkId);
        d.receiver = receiver;
        put(linkId, d);
    }

    public static synchronized void markTempReceiverIfEmpty(int linkId) {
        TeleportData d = getOrCreate(linkId);
        if (d.tempReceiver == null) d.tempReceiver = TeleportData.receiverApprox(linkId);
        put(linkId, d);
    }

    /* ---------- disk ---------- */

    public static synchronized void reloadConfigFile() {
        WorldServer overworld = DimensionManager.getWorld(0);
        if (overworld != null) {
            File saveFolder = overworld.getSaveHandler().getWorldDirectory();
            configFile = new File(saveFolder, "eaglemixins_teleports.json");
        } else {
            LOGGER.warn("[EagleMixins] World not loaded yet — defaulting to root.");
            configFile = new File("eaglemixins_teleports.json");
        }
    }

    private static synchronized void loadFromDisk() {
        if (configFile == null || !configFile.exists()) return;
        try (FileReader reader = new FileReader(configFile)) {
            Type type = new TypeToken<Map<Integer, TeleportData>>() {}.getType();
            Map<Integer, TeleportData> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                REGISTRY.clear();
                REGISTRY.putAll(loaded);
            }
        } catch (IOException e) {
            LOGGER.error("[EagleMixins] Failed to load teleport destinations: {}", e.getMessage());
        }
    }

    public static synchronized void saveToDisk() {
        if (configFile == null) return;
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(REGISTRY, writer);
        } catch (IOException e) {
            LOGGER.error("[EagleMixins] Failed to save teleport destinations: {}", e.getMessage());
        }
    }

    private static synchronized void seedMissingFromDefaults() {
        for (int linkId : TeleportData.ALL_IDS) {
            if (!REGISTRY.containsKey(linkId)) {
                TeleportData d = new TeleportData();
                d.sender = TeleportData.senderApprox(linkId);
                d.tempReceiver = TeleportData.receiverApprox(linkId);
                REGISTRY.put(linkId, d);
            }
        }
    }

}
