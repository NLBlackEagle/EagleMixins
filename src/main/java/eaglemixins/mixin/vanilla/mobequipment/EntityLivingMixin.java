package eaglemixins.mixin.vanilla.mobequipment;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.MobEquipmentConfig;
import eaglemixins.util.MobEquipState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(EntityLiving.class)
public abstract class EntityLivingMixin extends EntityLivingBase {

    public EntityLivingMixin(World worldIn) {
        super(worldIn);
    }

    @Unique private static MobEquipState eaglemixins$state = MobEquipState.OTHER;
    @Unique private static MobEquipmentConfig.ItemSetEntry eaglemixins$chosenSet = null;
    @Unique private static EntityLiving eaglemixins$currentEntity = null;

    @Inject(
            method = "setEquipmentBasedOnDifficulty",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/EntityEquipmentSlot;values()[Lnet/minecraft/inventory/EntityEquipmentSlot;")
    )
    private void eaglemixins_vanillaEntityLiving_setEquipmentBasedOnDifficulty_beforeLoop(DifficultyInstance p_180481_1_, CallbackInfo ci){
        eaglemixins$state = MobEquipState.START_EQUIPPING;
        eaglemixins$currentEntity = (EntityLiving)(Object) this;
    }

    @Inject(
            method = "setEquipmentBasedOnDifficulty",
            at = @At(value = "RETURN")
    )
    private void eaglemixins_vanillaEntityLiving_setEquipmentBasedOnDifficulty_tail(DifficultyInstance p_180481_1_, CallbackInfo ci){
        eaglemixins$chosenSet = null;
        eaglemixins$currentEntity = null;
        eaglemixins$state = MobEquipState.OTHER;
    }

    @Inject(
            method = "getArmorByChance",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void eaglemixins_vanillaEntityLiving_getArmorByChance(EntityEquipmentSlot slotIn, int chance, CallbackInfoReturnable<Item> cir){
        switch (eaglemixins$state){
            case START_EQUIPPING: // called first time for one mob from EntityLiving.setEquipmentBasedOnDifficulty
                eaglemixins$chosenSet = MobEquipmentConfig.getRandomArmor(eaglemixins$currentEntity.getRNG(), chance, true);
                cir.setReturnValue(eaglemixins$chosenSet.getItemForSlot(slotIn));
                eaglemixins$currentEntity.setDropChance(slotIn, eaglemixins$chosenSet.dropChance);
                eaglemixins$state = MobEquipState.SET_CHOSEN;
                return;
            case SET_CHOSEN: // called a second/third/fourth time for the same mob from EntityLiving.setEquipmentBasedOnDifficulty -> use same set
                cir.setReturnValue(eaglemixins$chosenSet.getItemForSlot(slotIn));
                eaglemixins$currentEntity.setDropChance(slotIn, eaglemixins$chosenSet.dropChance);
                return;
            case OTHER: // called from somewhere else -> random mix of sets that don't have dropchance 0 cause we cant actually apply it here
                cir.setReturnValue(MobEquipmentConfig.getRandomArmor(new Random(), chance, false).getItemForSlot(slotIn));
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
