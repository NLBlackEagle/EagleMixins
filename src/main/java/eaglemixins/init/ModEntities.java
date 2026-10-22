package eaglemixins.init;

import eaglemixins.EagleMixins;
import eaglemixins.entity.EntityPlayerBoss;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public final class ModEntities {

    /** Called from preInit. Network id 128 is EntitySpawnListener. */
    public static void init() {
        EntityRegistry.registerModEntity(
                new ResourceLocation(EagleMixins.MODID, "player_boss"),
                EntityPlayerBoss.class,
                "eaglemixins.player_boss",
                129,
                EagleMixins.INSTANCE,
                80,
                3,
                true
        );
    }

    private ModEntities() {}
}
