package eaglemixins.mixin.nuclearcraft;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import nc.item.armor.ItemHazmatSuit;
import nc.item.armor.NCItemArmor;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ISpecialArmor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemHazmatSuit.class)
public abstract class ItemHazmatSuitUnblockableMixin extends NCItemArmor implements ISpecialArmor {

    public ItemHazmatSuitUnblockableMixin(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn, String... tooltip) {
        super(materialIn, renderIndexIn, equipmentSlotIn, tooltip);
    }

    // For vanilla unblockables like poison
    @ModifyReturnValue(
            method = "handleUnblockableDamage",
            at = @At("RETURN"),
            remap = false
    )
    public boolean eaglemixins_ncItemHazmatSuit_handleUnblockableDamage(boolean original, @Local(argsOnly = true) DamageSource source){
        return original || source.isUnblockable();
    }
}
