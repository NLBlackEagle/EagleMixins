package eaglemixins.mixin.simpledifficulty;

import com.charles445.simpledifficulty.api.SDFluids;
import com.charles445.simpledifficulty.api.item.IItemCanteen;
import com.charles445.simpledifficulty.api.thirst.ThirstEnum;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * SimpleDifficulty fills canteens from an Inspirations cauldron through its own cauldron recipe, which only accepts
 * vanilla water (boiling = purified dose, otherwise normal). A cauldron filled with SimpleDifficulty's purified water
 * did nothing. This also accepts purified water and adds a purified dose; the recipe lowers the cauldron level by one as usual.
 */
@Mixin(targets = "com.charles445.simpledifficulty.compat.mod.InspirationsHandler$CanteenRecipe", remap = false)
public abstract class InspirationsCanteenRecipeMixin {
    @Inject(
            method = "matches",
            at = @At(value = "FIELD", target = "Lnet/minecraftforge/fluids/FluidRegistry;WATER:Lnet/minecraftforge/fluids/Fluid;"),
            cancellable = true
    )
    private void eaglemixins$acceptPurifiedWater(CallbackInfoReturnable<Boolean> cir,
                                                 @Local(argsOnly = true) ItemStack stack,
                                                 @Local IItemCanteen canteen,
                                                 @Local Fluid fluid) {
        if (fluid != null && fluid == SDFluids.purifiedWater) {
            cir.setReturnValue(canteen.tryAddDose(stack, ThirstEnum.PURIFIED));
        }
    }
}
