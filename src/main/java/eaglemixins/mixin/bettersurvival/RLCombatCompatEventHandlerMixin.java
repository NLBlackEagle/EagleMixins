package eaglemixins.mixin.bettersurvival;

import com.llamalad7.mixinextras.sugar.Local;
import com.mujmajnkraft.bettersurvival.integration.RLCombatCompatEventHandler;
import eaglemixins.EagleMixins;
import eaglemixins.util.DisarmingUtility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RLCombatCompatEventHandler.class)
public class RLCombatCompatEventHandlerMixin {
    @ModifyArg(
            method = "onDamageModifyPost",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/EntityItem;")
    )
    private ItemStack stopDisarmingGoodStuff_disarm(ItemStack stack, @Local EntityLivingBase target){
        EagleMixins.LOGGER.info("Disarmed");
        if(!DisarmingUtility.isAllowedToDisarm(stack, target))
            return ItemStack.EMPTY;
        else
            return stack;
    }

    @ModifyArg(
            method = "onDamageModifyPost",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;entityDropItem(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/item/EntityItem;")
    )
    private ItemStack stopDisarmingGoodStuff_handDropChances(ItemStack stack, @Local EntityLivingBase target){
        if(!DisarmingUtility.isAllowedToDisarm(stack, target))
            return ItemStack.EMPTY;
        else
            return stack;
    }
}
