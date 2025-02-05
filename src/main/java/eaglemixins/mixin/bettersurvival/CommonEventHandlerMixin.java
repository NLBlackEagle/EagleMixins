package eaglemixins.mixin.bettersurvival;

import com.llamalad7.mixinextras.sugar.Local;
import com.mujmajnkraft.bettersurvival.eventhandlers.CommonEventHandler;
import eaglemixins.util.DisarmingUtility;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CommonEventHandler.class)
public class CommonEventHandlerMixin {
    @ModifyArg(
            method = "onDamage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/EntityItem;")
    )
    private ItemStack stopDisarmingGoodStuff_disarm(ItemStack stack, @Local(argsOnly = true) LivingHurtEvent event){
        if(!DisarmingUtility.isAllowedToDisarm(stack, event.getEntityLiving()))
            return ItemStack.EMPTY;
        else
            return stack;
    }

    @ModifyArg(
            method = "onDamage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;entityDropItem(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/item/EntityItem;")
    )
    private ItemStack stopDisarmingGoodStuff_handDropChances(ItemStack stack, @Local(argsOnly = true) LivingHurtEvent event){
        if(!DisarmingUtility.isAllowedToDisarm(stack, event.getEntityLiving()))
            return ItemStack.EMPTY;
        else
            return stack;
    }
}
