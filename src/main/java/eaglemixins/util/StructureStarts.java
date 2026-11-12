package eaglemixins.util;

import eaglemixins.EagleMixins;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

/** Read access to MapGenStructure#structureMap (protected): every structure start that has been planned so far */
public final class StructureStarts {

    @Nullable private static final Field STRUCTURE_MAP = find();

    @Nullable
    private static Field find() {
        try {
            return ObfuscationReflectionHelper.findField(MapGenStructure.class, "field_75053_d");
        } catch (Exception e) {
            EagleMixins.LOGGER.error("Could not access MapGenStructure#structureMap, OTG won't avoid strongholds", e);
            return null;
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static Long2ObjectMap<StructureStart> get(MapGenStructure gen) {
        if (STRUCTURE_MAP == null || gen == null) return null;
        try {
            return (Long2ObjectMap<StructureStart>) STRUCTURE_MAP.get(gen);
        } catch (Exception e) {
            return null;
        }
    }

    private StructureStarts() {}
}
