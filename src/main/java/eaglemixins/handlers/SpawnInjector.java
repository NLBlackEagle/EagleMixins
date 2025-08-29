// SpawnInjector.java
package eaglemixins.handlers;

import eaglemixins.EagleMixins;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class SpawnInjector {
    private static final ResourceLocation VOLCANIC_ISLAND = new ResourceLocation("biomesoplenty", "volcanic_island");

    public static void inject() {
        Biome biome = ForgeRegistries.BIOMES.getValue(VOLCANIC_ISLAND);
        if (biome == null) {
            EagleMixins.LOGGER.warn("[SpawnInjector] Biome {} not found. Is Biomes O' Plenty loaded?", VOLCANIC_ISLAND);
            return;
        }

        add(biome, "lycanitesmobs:cinder",     120, 1, 3);
        add(biome, "lycanitesmobs:volcan",     100, 1, 2);
        add(biome, "lycanitesmobs:khalk",       90, 1, 2);
        add(biome, "lycanitesmobs:salamander",  80, 1, 2);
        add(biome, "lycanitesmobs:lobber",      80, 1, 2);
        add(biome, "lycanitesmobs:gorger",      80, 1, 2);
        add(biome, "lycanitesmobs:afrit",      100, 1, 2);
    }

    @SuppressWarnings("unchecked")
    private static void add(Biome biome, String entityId, int weight, int min, int max) {
        EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityId));
        if (entry == null) {
            EagleMixins.LOGGER.warn("[SpawnInjector] Entity {} not found, skipping.", entityId);
            return;
        }
        Class<?> cls = entry.getEntityClass();
        if (!EntityLiving.class.isAssignableFrom(cls)) {
            EagleMixins.LOGGER.warn("[SpawnInjector] Entity {} is not EntityLiving, skipping.", entityId);
            return;
        }
        biome.getSpawnableList(EnumCreatureType.MONSTER)
                .add(new Biome.SpawnListEntry((Class<? extends EntityLiving>) cls, weight, min, max));
    }

    private SpawnInjector() {}
}
