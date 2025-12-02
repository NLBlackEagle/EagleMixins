package eaglemixins.mixin.xat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import eaglemixins.handlers.RandomTpCancelHandler;
import eaglemixins.util.Ref;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xzeroair.trinkets.traits.abilities.AbilityEnderQueen;

@Mixin(AbilityEnderQueen.class)
public abstract class EnderQueensCrownRandomTpCancel {
    @ModifyExpressionValue(
            method = "attacked",
            at = @At(value = "INVOKE", target = "Lxzeroair/trinkets/traits/abilities/AbilityEnderQueen;teleportRandomly(Lnet/minecraft/entity/EntityLivingBase;)Z"),
            remap = false
    )
    private boolean eaglemixins_trinketsAndBaubles_attacked(boolean original, @Local(argsOnly = true) EntityLivingBase entity){
        if(!original) return false; //If tp failed for whatever reason, we don't punish (yet)
        if(entity.world.isRemote) return true;
        if (!(entity instanceof EntityPlayer)) return true;
        if (!RandomTpCancelHandler.isTpMethodEnabled("enderCrown")) return true;
        if (!Ref.entityIsInAbyssalRift(entity)) return true;
        RandomTpCancelHandler.applyTpCooldownDebuffs((EntityPlayer) entity);
        return true;
    }
}
