package eaglemixins.mixin.vanilla.mobequipment;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eaglemixins.compat.ModLoadedUtil;
import eaglemixins.compat.SpartanWeaponryUtil;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.handlers.RandomTippedArrowHandler;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityLiving.class)
public abstract class EntityLivingMixin_PickupArrows extends EntityLivingBase {
    public EntityLivingMixin_PickupArrows(World worldIn) {
        super(worldIn);
    }

    @ModifyReturnValue(
            method = "getSlotForItemStack",
            at = @At(value = "RETURN", ordinal = 3)
    )
    private static EntityEquipmentSlot eaglemixins_vanillaEntityLiving_getSlotForItemStack_allowTippedArrows(EntityEquipmentSlot original, ItemStack stack){
        if(original != EntityEquipmentSlot.MAINHAND) return original;
        //Put tipped arrows + tipped bolts in offhand
        if(
                RandomTippedArrowHandler.isValidTippedItem(stack.getItem(), true) ||
                RandomTippedArrowHandler.isValidTippedItem(stack.getItem(), false)
        )
            return EntityEquipmentSlot.OFFHAND;
        return original;
    }

    @ModifyExpressionValue(
            method = "updateEquipmentIfNeeded",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLiving;canEquipItem(Lnet/minecraft/item/ItemStack;)Z")
    )
    //inserts where the entity already decided its not worth picking up
    private boolean eaglemixins_vanillaEntityLiving_updateEquipmentIfNeeded_allowTippedArrows(boolean canEquip, @Local(name = "itemstack") ItemStack newStack, @Local EntityEquipmentSlot slot){
        if(!canEquip) return false; //will basically never happen, only vanilla instance is chicken jockey picking up eggs

        if(slot == EntityEquipmentSlot.OFFHAND) { //in vanilla this filters out everything except for shields and due to this mixin also tipped items
            PotionType type = PotionUtils.getPotionFromItem(newStack);

            //Only pickup tipped items with potion types from the random tipped arrow config (this is a somewhat broad check. all offhand items with potiontypes in their nbt will fall into this)
            if(!ForgeConfigHandler.tippedarrows.isRandomArrowPotionType(type)) return false;

            ItemStack main = this.getHeldItemMainhand();
            if(main.isEmpty()) return false;
            boolean hasBow = main.getItem() instanceof ItemBow; //includes spartan longbows
            boolean hasCrossbow = ModLoadedUtil.spartanweaponry.isLoaded() && SpartanWeaponryUtil.isSpartanCrossbow(main.getItem());
            if(!hasBow && !hasCrossbow) return false; //only pickup if main is bow or crossbow

            //Only pickup if the tipped item matches the mainhand bow (=arrow) or crossbow (=bolt)
            return RandomTippedArrowHandler.isValidTippedItem(newStack.getItem(), hasCrossbow);
        }

        return canEquip;
    }
}
