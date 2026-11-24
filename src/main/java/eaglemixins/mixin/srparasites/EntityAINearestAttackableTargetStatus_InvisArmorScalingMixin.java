package eaglemixins.mixin.srparasites;

import com.dhanantry.scapeandrunparasites.entity.ai.EntityAINearestAttackableTargetStatus;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Parasites look for players with this AI. For invisible players SRP multiplies the detection range by
 * "Invisible Penalty" * armor visibility (worn armor slots / 4, min 0.1), so without armor the range drops to 10%,
 * even with the penalty set to 1.0.
 * This skips SRP's multiplier and instead scales the range by floor + (1 - floor) * worn armor slots / 4,
 * so an invisible player without armor is still detected at "floor" of the normal range, and with full armor at the full range.
 * The sneak penalty is applied before this and still stacks.
 */
@Mixin(EntityAINearestAttackableTargetStatus.class)
public abstract class EntityAINearestAttackableTargetStatus_InvisArmorScalingMixin {
    @ModifyExpressionValue(
            method = "getNearest",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isInvisible()Z", remap = true),
            remap = false
    )
    private boolean eagleMixins_srpNearestTargetStatus_skipSRPInvisPenalty(boolean isInvisible) {
        return false;
    }

    @WrapOperation(
            method = "getNearest",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;getPlayerVisibilityDistance(Lnet/minecraft/entity/player/EntityPlayer;DD)D", remap = false),
            remap = false
    )
    private double eagleMixins_srpNearestTargetStatus_invisArmorScaling(EntityPlayer player, double range, double maxYDistance, Operation<Double> original) {
        if (player.isInvisible()) {
            double floor = MathHelper.clamp(ForgeConfigHandler.srparasites.invisibleDetectionFloor, 0.0, 1.0);
            range *= floor + (1.0 - floor) * player.getArmorVisibility();
        }
        return original.call(player, range, maxYDistance);
    }
}
