package eaglemixins.config.folders;

import eaglemixins.EagleMixins;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LockedBlockConfig {

    @Config.Comment({
            "Lock types. Keys (eaglemixins:lock_key) and lock blocks (eaglemixins:locked_block) are paired through the LockId NBT tag:",
            "a key only opens locks with the same LockId and is used up when it opens one.",
            "Keyslaves (eaglemixins:keyslave) touching an opened lock, or touching those keyslaves, vanish together with it.",
            "Format: <lock id> | <name> | <key color> | <key lore> | <lock lore>",
            "Lock id: any text, used in the NBT. Name: shown as \"<Name> Key\" / \"<Name> Lock\". Key color: hex, e.g. #8B1E1E",
            "Key lore: tooltip of the key. Lock lore: message shown when right-clicking the lock without its key.",
            "Lore can use \\n for new lines and \u00a7 color codes, and can be left empty. Only the lock id is required.",
            "Locks and keys with a LockId that is not listed here still work, they just use default names and no lore.",
            "Examples:",
            "    /give @p eaglemixins:lock_key 1 0 {LockId:\"brutal_arachnid\"}",
            "    /give @p eaglemixins:locked_block 1 0 {BlockEntityTag:{LockId:\"brutal_arachnid\"}}",
            "    loot table function: {\"function\": \"set_nbt\", \"tag\": \"{LockId:\\\"brutal_arachnid\\\"}\"}",
            "In creative, right-clicking a lock that has no LockId with a key gives the lock that key's LockId."
    })
    @Config.Name("Locks")
    public String[] locks = {
            "brutal_arachnid | Arachnid | #9d9684 | \u00a77Pulled from the remains of the great spider. | \u00a77Thick webbing is woven through the keyhole.\\n\u00a77Only one who has faced the brood may pass.",
            "brutal_gatekeeper | Gatekeeper | #865ec9 | \u00a77Torn from the storm dragon that guarded the way. | \u00a77Static crackles across the keyhole.\\n\u00a77The Gatekeeper holds the key to what lies beyond.",
            "brutal_sarevok | Sarevok's | #b7812c | \u00a77Taken from Sarevok's fallen body. | \u00a77Deep sword gouges scar the stone around the keyhole.\\n\u00a77The horned one does not give up his key willingly.",
            "brutal_horror | Parasitic | #6a9834 | \u00a77Cut free from the horror's writhing mass. | \u00a77Fleshy growths pulse around the keyhole.\\n\u00a77Whatever opens this was grown, not forged.",
            "brutal_necrotic_golem | Necrotic | #31352c | \u00a77Pried from the golem's crumbling core. | \u00a77The stone here is grey and dead, and smells of the grave.\\n\u00a77Its key lies within a heart of rotting stone.",
            "brutal_bogle | Dread | #e5dd05 | \u00a77Left behind when the bogle finally fell silent. | \u00a77Faint scratch marks circle the keyhole.\\n\u00a77Only one who has silenced the bogle may enter.",
            "brutal_dev | Developer's | #d42300 | \u00a77Wrestled from the developers after a long crunch. | \u00a77Words are scratched beside the keyhole: \"TODO: Phase 2/4\".\\n\u00a77The developers keep the key. Go and ask them nicely.",
            "brutal_shivaxi | Shivaxi's | #00c8d4 | \u00a77Taken from Shivaxi, altered by the Eagle. | \u00a77A blinding light seeps through the keyhole.\\n\u00a77This is where the new world begins."
    };

    @Config.Comment({
            "Hardness of lock blocks and keyslaves. -1 = unbreakable (like bedrock), 50 = obsidian.",
            "They never drop themselves when broken."
    })
    @Config.Name("Hardness")
    @Config.RequiresMcRestart
    @Config.RangeDouble(min = -1)
    public float hardness = -1;

    @Config.Comment("If true, opening a lock also opens all locks with the same LockId touching it")
    @Config.Name("Open Connected Locks")
    public boolean openConnected = true;

    @Config.Comment("Max amount of blocks (locks and keyslaves) opened by one key")
    @Config.Name("Max Connected Locks")
    @Config.RangeInt(min = 1, max = 4096)
    public int maxConnected = 64;

    @Config.Comment("Ticks between each block vanishing when a lock is opened (20 ticks = 1 second). 0 = all at once")
    @Config.Name("Vanish Delay")
    @Config.RangeInt(min = 0, max = 200)
    public int vanishDelay = 10;

    @Config.Comment("If true, keys are not used up in creative mode")
    @Config.Name("Keep Key In Creative")
    public boolean keepKeyInCreative = true;

    private Map<String, LockType> lockTypes = null;

    @Nullable
    public LockType getLockType(String id) {
        return getLockTypeMap().get(id);
    }

    public Collection<LockType> getLockTypes() {
        return getLockTypeMap().values();
    }

    private Map<String, LockType> getLockTypeMap() {
        if (lockTypes == null) {
            lockTypes = new LinkedHashMap<>();
            for (String entry : locks) {
                String[] parts = entry.split("\\|", -1);
                String id = parts[0].trim();
                if (id.isEmpty()) continue;
                String name = parts.length > 1 ? parts[1].trim() : "";
                int color = 0xFFFFFF;
                if (parts.length > 2 && !parts[2].trim().isEmpty()) {
                    try {
                        color = Integer.parseInt(parts[2].trim().replace("#", ""), 16);
                    } catch (NumberFormatException ex) {
                        EagleMixins.LOGGER.error("Invalid key color \"{}\" for lock \"{}\" in Locked Blocks config", parts[2].trim(), id);
                    }
                }
                List<String> keyLore = parts.length > 3 ? lore(parts[3]) : Collections.emptyList();
                List<String> lockLore = parts.length > 4 ? lore(parts[4]) : Collections.emptyList();
                lockTypes.put(id, new LockType(id, name, color, keyLore, lockLore));
            }
        }
        return lockTypes;
    }

    private static List<String> lore(String text) {
        text = text.trim();
        return text.isEmpty() ? Collections.emptyList() : Arrays.asList(text.split("\\\\n"));
    }

    public void reset() {
        lockTypes = null;
    }

    public static class LockType {
        public final String id;
        public final String name; // empty = derived from the id
        public final int color;
        public final List<String> keyLore;
        public final List<String> lockLore;

        public LockType(String id, String name, int color, List<String> keyLore, List<String> lockLore) {
            this.id = id;
            this.name = name;
            this.color = color;
            this.keyLore = keyLore;
            this.lockLore = lockLore;
        }
    }
}
