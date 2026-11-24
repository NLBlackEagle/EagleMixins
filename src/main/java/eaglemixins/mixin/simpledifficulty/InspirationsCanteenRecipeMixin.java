package eaglemixins.mixin.simpledifficulty;

import com.charles445.simpledifficulty.api.SDFluids;
import com.charles445.simpledifficulty.api.item.IItemCanteen;
import com.charles445.simpledifficulty.api.thirst.ThirstEnum;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * SimpleDifficulty fills canteens from an Inspirations cauldron through its own cauldron recipe, which adds the dose in matches().
 * - It only accepted vanilla water (boiling = purified dose, otherwise normal). This also accepts SimpleDifficulty's purified water.
 * - tryAddDose fills a canteen completely for a normal dose (meant for water sources), so one cauldron level filled a whole canteen.
 *   Here every cauldron level gives exactly one dose, matching the one level a dose adds when poured back in.
 * - A canteen that holds the other kind of water is refused, so purified doses are never turned into normal ones.
 * The recipe lowers the cauldron level by one per dose as usual.
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
            cir.setReturnValue(eaglemixins$addOneDose(canteen, stack, ThirstEnum.PURIFIED));
        }
    }

    @WrapOperation(
            method = "matches",
            at = @At(value = "INVOKE", target = "Lcom/charles445/simpledifficulty/api/item/IItemCanteen;tryAddDose(Lnet/minecraft/item/ItemStack;Lcom/charles445/simpledifficulty/api/thirst/ThirstEnum;)Z")
    )
    private boolean eaglemixins$oneDosePerLevel(IItemCanteen canteen, ItemStack stack, ThirstEnum type, Operation<Boolean> original) {
        return eaglemixins$addOneDose(canteen, stack, type);
    }

    @Unique
    private static boolean eaglemixins$addOneDose(IItemCanteen canteen, ItemStack stack, ThirstEnum type) {
        int doses = Math.max(0, canteen.getDoses(stack));
        if (doses > 0 && canteen.getThirstEnum(stack) != type) return false; // don't mix purified and normal water
        if (doses >= canteen.getMaxDoses(stack)) return false;
        canteen.setDoses(stack, type, doses + 1);
        return true;
    }
}
