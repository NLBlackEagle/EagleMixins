package eaglemixins.mixin.vanilla;

import net.minecraft.client.renderer.tileentity.TileEntityMobSpawnerRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.EnumFacing;
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
        World world = mobSpawnerLogic.getSpawnerWorld();
        BlockPos pos = mobSpawnerLogic.getSpawnerPosition();

        if (eagleMixins$isOccluded(world, pos)) ci.cancel();
    }

    /**
     * Only checks the up-to-3 faces that could actually be facing the camera,
     * instead of blindly requiring all 6 neighbors to be opaque.
     */
    @Unique
    private static boolean eagleMixins$isOccluded(World world, BlockPos pos) {
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos(pos);

        if (TileEntityRendererDispatcher.staticPlayerX < pos.getX()) {
            if (eagleMixins$isFaceVisible(world, neighbor, EnumFacing.WEST)) return false;
        } else if (TileEntityRendererDispatcher.staticPlayerX > pos.getX() + 1) {
            if (eagleMixins$isFaceVisible(world, neighbor, EnumFacing.EAST)) return false;
        }

        if (TileEntityRendererDispatcher.staticPlayerY < pos.getY()) {
            if (eagleMixins$isFaceVisible(world, neighbor, EnumFacing.DOWN)) return false;
        } else if (TileEntityRendererDispatcher.staticPlayerY > pos.getY() + 1) {
            if (eagleMixins$isFaceVisible(world, neighbor, EnumFacing.UP)) return false;
        }

        if (TileEntityRendererDispatcher.staticPlayerZ < pos.getZ()) {
            if (eagleMixins$isFaceVisible(world, neighbor, EnumFacing.NORTH)) return false;
        } else if (TileEntityRendererDispatcher.staticPlayerZ > pos.getZ() + 1) {
            if (eagleMixins$isFaceVisible(world, neighbor, EnumFacing.SOUTH)) return false;
        }

        return true;
    }

    @Unique
    private static boolean eagleMixins$isFaceVisible(World world, BlockPos.MutableBlockPos pos, EnumFacing face) {
        pos.move(face);
        boolean visible = !world.getBlockState(pos).doesSideBlockRendering(world, pos, face.getOpposite());
        pos.move(face.getOpposite());
        return visible;
    }
}
