package eaglemixins.handlers;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Mod.EventBusSubscriber
public class BiomeTagHandler {

    public static void init() {

        final Biome biome = ForgeRegistries.BIOMES.getValue(
                new ResourceLocation("nuclearcraft", "nuclear_wasteland")
        );

        if (biome != null) {
            BiomeDictionary.addTypes(biome, BiomeDictionary.Type.getType("NUCLEAR"));
        }
    }
}
