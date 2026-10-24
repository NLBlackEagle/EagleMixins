package eaglemixins.client.render;

import eaglemixins.client.AmbientLightningHandler;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.client.renderer.entity.RenderLightningBolt;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/** Real lightning striking in a wasteland biome is drawn as a wasteland bolt instead of the white vanilla one */
public class RenderWastelandLightning extends RenderLightningBolt {
    private static final Set<EntityLightningBolt> REPLACED = Collections.newSetFromMap(new WeakHashMap<>());

    public RenderWastelandLightning(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityLightningBolt entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (REPLACED.contains(entity)) return;
        if (ForgeConfigHandler.lightning.wastelandReplaceVanilla
                && ForgeConfigHandler.lightning.isWastelandBiome(entity.world.getBiome(entity.getPosition()).getRegistryName())) {
            // the bolt entity plays its own thunder, so the replacement stays silent
            AmbientLightningHandler.skyBoltAt(entity.world, new Vec3d(entity.posX, entity.posY, entity.posZ), 1.0F, 0);
            REPLACED.add(entity);
            return;
        }
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
