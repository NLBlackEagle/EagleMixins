package eaglemixins.mixin.vanilla;

import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityMobSpawnerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityMobSpawnerRenderer.class)
public abstract class TileEntityMobSpawnerRendererMixin {

    @Inject(
            method = "renderMob",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void disableSpawnerMob(
            MobSpawnerBaseLogic mobSpawnerLogic,
            double posX, double posY,
            double posZ,
            float partialTicks,
            CallbackInfo ci
    ) {
        if (!ForgeConfigHandler.client.cullSpawnerRendering) {
            return; // feature off, vanilla behavior
        }

        World world = mobSpawnerLogic.getSpawnerWorld();
        BlockPos pos = mobSpawnerLogic.getSpawnerPosition();

        // 1. distance cull — skip mobs rendering past a configurable radius
        Entity view = Minecraft.getMinecraft().getRenderViewEntity();
        if (view != null) {
            double dx = posX - view.posX;
            double dy = posY - view.posY;
            double dz = posZ - view.posZ;
            double distSq = dx * dx + dy * dy + dz * dz;
            double maxDist = ForgeConfigHandler.client.spawnerRenderDistance;
            if (distSq > maxDist * maxDist) {
                ci.cancel();
                return;
            }
        }

        // 2. occlusion cull — skip if fully sealed in opaque blocks
        if (ForgeConfigHandler.client.cullEnclosedSpawners && eagleMixins$isFullyEnclosed(world, pos)) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean eagleMixins$isFullyEnclosed(World world, BlockPos pos) {
        return world.getBlockState(pos.up()).isOpaqueCube()
                && world.getBlockState(pos.down()).isOpaqueCube()
                && world.getBlockState(pos.north()).isOpaqueCube()
                && world.getBlockState(pos.south()).isOpaqueCube()
                && world.getBlockState(pos.east()).isOpaqueCube()
                && world.getBlockState(pos.west()).isOpaqueCube();
    }
}