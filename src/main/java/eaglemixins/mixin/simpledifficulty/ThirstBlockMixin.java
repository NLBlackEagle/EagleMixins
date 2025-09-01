package eaglemixins.mixin.simpledifficulty;

import com.charles445.simpledifficulty.api.thirst.ThirstEnum;
import com.charles445.simpledifficulty.util.internal.ThirstUtilInternal;
import eaglemixins.config.ForgeConfigHandler;
import com.charles445.simpledifficulty.api.thirst.ThirstEnumBlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;
import java.util.Objects;

@Mixin(ThirstUtilInternal.class)
public class ThirstBlockMixin {

    @Inject(method = "traceWater", at = @At("RETURN"), cancellable = true, remap = false)
    private void eaglemixins$extendDrinkableBlocks(EntityPlayer player, CallbackInfoReturnable<ThirstEnumBlockPos> cir) {
        // If vanilla method already returned something valid, leave it
        if (cir.getReturnValue() != null) return;

        // Ray trace again to check custom blocks
        double reach = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() * 0.5;
        Vec3d eyevec = player.getPositionEyes(1.0F);
        Vec3d lookvec = player.getLook(1.0F);
        Vec3d targetvec = eyevec.add(lookvec.x * reach, lookvec.y * reach, lookvec.z * reach);
        RayTraceResult trace = player.world.rayTraceBlocks(eyevec, targetvec, true);

        if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = trace.getBlockPos();
            IBlockState state = player.world.getBlockState(pos);
            Block block = state.getBlock();

            // Check against config list
            String name = Objects.requireNonNull(block.getRegistryName()).toString();
            if (ForgeConfigHandler.cachedDrinkableBlocks.contains(name.toLowerCase(Locale.ROOT))) {
                cir.setReturnValue(new ThirstEnumBlockPos(ThirstEnum.NORMAL, pos));
            }
        }
    }
}