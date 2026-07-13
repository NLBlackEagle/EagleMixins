package eaglemixins.mixin.vanilla;

import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(TileEntityMobSpawner.class)
public abstract class TileEntityMobSpawnerMixin extends TileEntity {

    @Override
    public double getMaxRenderDistanceSquared() {
        if (!ForgeConfigHandler.client.cullSpawnerRendering) {
            return super.getMaxRenderDistanceSquared(); // feature off, vanilla behavior
        }

        double maxDist = ForgeConfigHandler.client.spawnerRenderDistance;
        return maxDist * maxDist;
    }
}

