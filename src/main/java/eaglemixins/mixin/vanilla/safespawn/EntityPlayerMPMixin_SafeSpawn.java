package eaglemixins.mixin.vanilla.safespawn;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eaglemixins.handlers.util.SafeSpawnPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

/**
 * Every new EntityPlayerMP (joining, respawning) starts at a random spawn point. The point is remembered,
 * so SafeSpawnHandler can tell whether the player actually stayed there (no saved position or bed).
 */
@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin_SafeSpawn implements SafeSpawnPlayer {

    @Unique
    @Nullable
    private BlockPos eaglemixins$randomSpawn;

    @ModifyExpressionValue(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;getRandomizedSpawnPoint()Lnet/minecraft/util/math/BlockPos;", remap = false)
    )
    private BlockPos eaglemixins_rememberRandomSpawn(BlockPos pos) {
        eaglemixins$randomSpawn = pos;
        return pos;
    }

    @Override
    @Nullable
    public BlockPos eaglemixins$getRandomSpawn() {
        return eaglemixins$randomSpawn;
    }
}
