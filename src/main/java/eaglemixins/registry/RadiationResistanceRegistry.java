package eaglemixins.registry;

import eaglemixins.EagleMixins;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class RadiationResistanceRegistry {

    private static final Map<ResourceLocation, Double> MAP = new HashMap<>();

    private RadiationResistanceRegistry() {}

    public static void reloadFromConfig() {
        MAP.clear();

        if (ForgeConfigHandler.server.radiationResistanceList == null) {
            EagleMixins.LOGGER.info("[EagleMixins] Radiation Immunity list empty, nothing loaded");
            return;
        }

        for (String raw : ForgeConfigHandler.server.radiationResistanceList) {
            if (raw == null) continue;
            String s = raw.trim();
            if (s.isEmpty()) continue;

            // Prefer '=', fallback to last '_' so IDs with underscores still work.
            int sep = s.lastIndexOf('=');
            if (sep < 0) sep = s.lastIndexOf('_');

            if (sep <= 0 || sep >= s.length() - 1) {
                EagleMixins.LOGGER.error("[EagleMixins] RadRes Invalid entry '{}', expected <id>=<val> or <id>_<val>", s);
                continue;
            }

            String idStr = s.substring(0, sep).trim();
            String valStr = s.substring(sep + 1).trim();

            try {
                ResourceLocation id = new ResourceLocation(idStr);
                double val = Double.parseDouble(valStr);
                MAP.put(id, val);
            } catch (Exception e) {
                EagleMixins.LOGGER.error("[EagleMixins] RadRes Failed to parse '{}': {}", s, e.toString());
            }
        }

        EagleMixins.LOGGER.info("[EagleMixins] RadRes Loaded {} entity resistance override(s)", MAP.size());
    }

    public static double get(ResourceLocation id) {
        return MAP.getOrDefault(id, 0.0D);
    }

}
