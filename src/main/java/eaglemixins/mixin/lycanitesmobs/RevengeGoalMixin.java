package eaglemixins.mixin.lycanitesmobs;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.targeting.RevengeGoal;
import com.lycanitesmobs.core.entity.goals.targeting.TargetingGoal;
import eaglemixins.handlers.RetaliationHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * RevengeGoal only fights back when one of the mob's creature groups says so ("retaliate"/"hunt").
 * Mobs on the EagleMixins Retaliation list also fight back against survival players, the groups can stay "wary".
 */
@Mixin(RevengeGoal.class)
public abstract class RevengeGoalMixin extends TargetingGoal {
    public RevengeGoalMixin(BaseCreatureEntity host) {
        super(host);
    }

    @ModifyExpressionValue(
            method = "shouldExecute",
            at = @At(value = "INVOKE", target = "Lcom/lycanitesmobs/core/info/CreatureGroup;shouldRevenge(Lnet/minecraft/entity/Entity;)Z", remap = false)
    )
    private boolean eagleMixins$retaliateAgainstPlayers(boolean shouldRevenge) {
        return shouldRevenge || RetaliationHandler.shouldFightPlayer(this.host, this.host.getRevengeTarget());
    }
}
