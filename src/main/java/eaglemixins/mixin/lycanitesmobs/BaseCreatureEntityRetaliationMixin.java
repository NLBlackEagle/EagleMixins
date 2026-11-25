package eaglemixins.mixin.lycanitesmobs;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import eaglemixins.handlers.RetaliationHandler;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Counterpart of {@link RevengeGoalMixin}: AvoidIfHitGoal ("wary") runs away whenever shouldCreatureGroupRevenge is false,
 * so for listed mobs hit by a survival player it has to say "revenge" too, otherwise they would fight and flee at once.
 */
@Mixin(BaseCreatureEntity.class)
public abstract class BaseCreatureEntityRetaliationMixin {

    @ModifyExpressionValue(
            method = "shouldCreatureGroupRevenge",
            at = @At(value = "INVOKE", target = "Lcom/lycanitesmobs/core/info/CreatureGroup;shouldRevenge(Lnet/minecraft/entity/Entity;)Z"),
            remap = false
    )
    private boolean eagleMixins$retaliateAgainstPlayers(boolean shouldRevenge, @Local(argsOnly = true) EntityLivingBase target) {
        return shouldRevenge || RetaliationHandler.shouldFightPlayer((BaseCreatureEntity) (Object) this, target);
    }
}
