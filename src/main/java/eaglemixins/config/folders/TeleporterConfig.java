package eaglemixins.config.folders;

import eaglemixins.EagleMixins;
import eaglemixins.teleport.TeleportData;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.config.Config;

import java.util.HashMap;
import java.util.Map;

public class TeleporterConfig {
    @Config.Comment("Main toggle for teleporter feature used in dregora to teleport players from various spots in the overworld to other spots.")
    @Config.Name("Enable Teleporters")
    @Config.RequiresMcRestart
    public boolean enableTeleporters = true;

    @Config.Comment("How long (seconds) to apply teleportation sickness after each teleport.")
    @Config.Name("Teleportation Sickness Duration")
    @Config.RangeInt(min = 0)
    public int sicknessDuration = 10;

    @Config.Comment("Chance of teleporting glitching out and sending player somewhere else.")
    @Config.Name("Teleportation Glitch Chance")
    @Config.RangeDouble(min = 0, max = 1)
    public double teleportationChance = 0.01;

    @Config.Comment("How long (seconds) a teleportation glitch will last minimally.")
    @Config.Name("Teleportation Glitch Duration Min")
    @Config.RangeInt(min = 0)
    public int glitchDurationMin = 7;

    @Config.Comment("How long (seconds) a teleportation glitch will last at the most.")
    @Config.Name("Teleportation Glitch Duration Max")
    @Config.RangeInt(min = 0)
    public int glitchDurationMax = 20;

    @Config.Comment({
            "Pattern (all integers): linkId, senderX, senderY, senderZ, receiverX, receiverY, receiverZ",
            "Changing this alone is not enough for Dregora Teleporter Structs, those need invisible \"SpawnListener\" entities to set the exact positions later on the structures chunk gen"
    })
    @Config.Name("Approximate Teleporter Positions")
    @Config.RequiresMcRestart
    public String[] teleporterLinks = {
            "0, -950, 80, -2228, -8247, 80, -12929",   // Frozen Greens
            "1, -2355, 80, -642, -15878, 80, -4301",   // The Highlands
            "2, -1987, 80, 1453, -13520, 80, 7636",    // Valley of Sulfur
            "3, -116, 80, 2455, -2607, 80, 16323",     // Southern Green
            "4, 2407, 80, -401, 13476, 80, -8398",     // Sea of Decay
            "5, 1171, 80, -2126, 7070, 80, -15844",    // Green Desert
            "6, 1831, 80, 1635, 5502, 80, 13754"       // Permafrost
    };

    private Map<Integer, TeleportData> parsedLinks = null;

    public Map<Integer, TeleportData> getApproxTeleporterLinks() {
        if (parsedLinks == null) {
            parsedLinks = new HashMap<>();
            for (String link : teleporterLinks) {
                String[] split = link.split(",");
                if (split.length < 7) {
                    EagleMixins.LOGGER.warn("Invalid teleporter link config: {}, skipping.", link);
                    continue;
                }
                try {
                    int linkId = Integer.parseInt(split[0].trim());
                    int senderX = Integer.parseInt(split[1].trim());
                    int senderY = Integer.parseInt(split[2].trim());
                    int senderZ = Integer.parseInt(split[3].trim());
                    int receiverX = Integer.parseInt(split[4].trim());
                    int receiverY = Integer.parseInt(split[5].trim());
                    int receiverZ = Integer.parseInt(split[6].trim());

                    parsedLinks.put(linkId, new TeleportData(
                            new BlockPos(senderX, senderY, senderZ),
                            new BlockPos(receiverX, receiverY, receiverZ)
                    ));
                } catch (NumberFormatException e) {
                    EagleMixins.LOGGER.warn("Failed to parse teleporter link config: {}, error: {}", link, e.getMessage());
                }
            }
        }
        return parsedLinks;
    }

    public void reset() {
        parsedLinks = null; // allowing this data to be updated is basically never useful, but it also doesn't really hurt
    }
}
