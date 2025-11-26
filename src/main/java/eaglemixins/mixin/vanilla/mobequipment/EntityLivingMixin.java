package eaglemixins.mixin.vanilla.mobequipment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.MobEquipmentConfig;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(EntityLiving.class)
public abstract class EntityLivingMixin extends EntityLivingBase {
    public EntityLivingMixin(World worldIn) {
        super(worldIn);
    }

    @Unique private static int eaglemixins$state = -1;
    @Unique private static MobEquipmentConfig.ItemSetEntry eaglemixins$chosenSet = null;
    @Unique private static Random eaglemixins$currentRand = null;

    @WrapOperation(
            method = "setEquipmentBasedOnDifficulty",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLiving;getArmorByChance(Lnet/minecraft/inventory/EntityEquipmentSlot;I)Lnet/minecraft/item/Item;")
    )
    private Item eaglemixins_vanillaEntityLiving_setEquipmentBasedOnDifficulty(EntityEquipmentSlot slotIn, int chance, Operation<Item> original){
        eaglemixins$state = 0;
        eaglemixins$currentRand = this.getRNG();
        Item item = original.call(slotIn, chance);
        eaglemixins$state = -1;
        eaglemixins$chosenSet = null;
        eaglemixins$currentRand = null;
        return item;
    }

    @Inject(
            method = "getArmorByChance",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void eaglemixins_vanillaEntityLiving_getArmorByChance(EntityEquipmentSlot slotIn, int chance, CallbackInfoReturnable<Item> cir){
        switch (eaglemixins$state){
            case 0: // called first time for one mob from EntityLiving.setEquipmentBasedOnDifficulty
                eaglemixins$chosenSet = MobEquipmentConfig.getRandomArmor(eaglemixins$currentRand, chance); //TODO chose an actual set
                cir.setReturnValue(eaglemixins$chosenSet.getItemForSlot(slotIn));
                eaglemixins$state = 1;
                return;
            case 1: // called a second/third/fourth time for the same mob from EntityLiving.setEquipmentBasedOnDifficulty -> use same set
                cir.setReturnValue(eaglemixins$chosenSet.getItemForSlot(slotIn));
                return;
            case -1: // called from somewhere else -> random mix of sets
                cir.setReturnValue(MobEquipmentConfig.getRandomArmor(eaglemixins$currentRand, chance).getItemForSlot(slotIn));
        }
    }

    @ModifyConstant(
            method = "setEquipmentBasedOnDifficulty",
            constant = @Constant(floatValue = 0.15F)
    )
    private float eaglemixins_vanillaEntityLiving_setEquipmentBasedOnDifficulty_base(float constant){
        return ForgeConfigHandler.mobequipment.baseArmorChance;
    }

    @ModifyConstant(
            method = "setEquipmentBasedOnDifficulty",
            constant = @Constant(floatValue = 0.095F)
    )
    private float eaglemixins_vanillaEntityLiving_setEquipmentBasedOnDifficulty_tierincreasechance(float constant){
        return 0; //fail all of them and instead do our own
    }

    @ModifyVariable(
            method = "setEquipmentBasedOnDifficulty",
            at = @At("STORE"),
            name = "i"
    )
    private int eaglemixins_vanillaEntityLiving_setEquipmentBasedOnDifficulty_maxtier(int constant){
        for(int idx = 1; idx < ForgeConfigHandler.mobequipment.armorMaxTier; idx++)
            if (this.getRNG().nextFloat() < ForgeConfigHandler.mobequipment.armorTierIncreaseChance)
                ++constant;
        return constant;
    }

    @ModifyVariable(
            method = "setEquipmentBasedOnDifficulty",
            at = @At("STORE"),
            name = "f"
    )
    private float eaglemixins_vanillaEntityLiving_setEquipmentBasedOnDifficulty_additionalArmorPieceChance(float constant) {
        return MathHelper.clamp(1F - ForgeConfigHandler.mobequipment.additionalArmorChanceMulti * (1F - constant), 0F, 1F);
    }
}
