package eaglemixins.handlers;

import eaglemixins.EagleMixins;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Map;

public class BiomeTagHandler {

    public static void init() {
        for (Map.Entry<ResourceLocation, ArrayList<String>> entry : ForgeConfigHandler.server.biomeDictionaryTagList.entrySet()) {
            try {
                Biome biome = ForgeRegistries.BIOMES.getValue(entry.getKey());
                if (biome == null) {
                    EagleMixins.LOGGER.warn("[EagleMixins] BiomeTag: biome '{}' not found, skipping", entry.getKey().toString());
                    continue;
                }
                entry.getValue().forEach(tag ->
                        BiomeDictionary.addTypes(biome, BiomeDictionary.Type.getType(tag))
                );
            } catch (Exception e) {
                EagleMixins.LOGGER.error("[EagleMixins] BiomeTag Failed to parse '{}': {}", entry.getKey(), e.toString());
            }
        }
    }
}
