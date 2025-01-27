package eaglemixins.mixin.srparasites;

import com.dhanantry.scapeandrunparasites.item.tool.WeaponToolMeleeBase;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeaponToolMeleeBase.class)
public class WeaponToolMeleeBaseMixin {
    @Inject(
            method = "onUpdate",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;ticksExisted:I", ordinal = 1),
            remap = false,
            cancellable = true
    )
    public void eagleMixins_srpWeaponToolMeleeBase_onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected, CallbackInfo ci){
        //Cancel srp evolving weapons to sentient
        ci.cancel();
    }

    @Inject(
            method = "hitEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;getHealth()F"),
            remap = false,
            cancellable = true
    )
    public void eagleMixins_srpWeaponToolMeleeBase_hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker, CallbackInfoReturnable<Boolean> cir, @Local boolean flag){
        //Cancel srp adding health to srpkills tag
        cir.setReturnValue(flag);
    }
}
