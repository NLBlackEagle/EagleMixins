package eaglemixins.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(TileEntity.class) // don't override in a mixin, inject in super and check for type
public abstract class TileEntityMobSpawnerMixin {

    @ModifyReturnValue(
            method = "getMaxRenderDistanceSquared",
            at = @At("RETURN")
    )
    public double eaglemixins_getMaxRenderDistanceSquared(double original) {
        TileEntity thisTile = (TileEntity)(Object)this;
        if(!(thisTile instanceof TileEntityMobSpawner)) return original;

        double maxDist = ForgeConfigHandler.client.spawnerRenderDistance;
        return maxDist * maxDist;
    }
}

