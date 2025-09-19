package eaglemixins.mixin.cookingforblockheads;

import com.charles445.simpledifficulty.api.SDItems;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.blay09.mods.cookingforblockheads.api.SourceItem;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenItemProvider;
import net.blay09.mods.cookingforblockheads.api.capability.IngredientPredicate;
import net.blay09.mods.cookingforblockheads.tile.TileFridge;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.blay09.mods.cookingforblockheads.tile.TileFridge$2")
public abstract class TileFridgeMixin {
    @Unique private final ItemStack eaglemixins$iceChunkStack = new ItemStack(SDItems.ice_chunk);
    @Shadow(remap = false) @Final TileFridge this$0; //arcane mixin method, referencing enclosing class from inner anonymous class

    @ModifyReturnValue(
            method = "applyIceUnit",
            at = @At("RETURN"),
            remap = false
    )
    private SourceItem eaglemixins_cookingforblockheadsTileFridge_applyIceUnit(SourceItem original, @Local(argsOnly = true) IngredientPredicate predicate, @Local(argsOnly = true) int maxAmount){
        if(original != null) return original;
        if(this$0.getBaseFridge().hasIceUpgrade && predicate.test(this.eaglemixins$iceChunkStack, 64))
            return new SourceItem((IKitchenItemProvider) this, -1, ItemHandlerHelper.copyStackWithSize(this.eaglemixins$iceChunkStack, maxAmount));
        return null;
    }
}
