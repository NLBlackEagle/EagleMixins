package eaglemixins.mixin.vanilla.skeletonmoddedbows;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.oblivioussp.spartanweaponry.entity.projectile.EntityBolt;
import com.oblivioussp.spartanweaponry.entity.projectile.EntityBoltSpectral;
import com.oblivioussp.spartanweaponry.entity.projectile.EntityBoltTipped;
import com.oblivioussp.spartanweaponry.init.ItemRegistrySW;
import com.oblivioussp.spartanweaponry.init.SoundRegistry;
import com.oblivioussp.spartanweaponry.item.ItemBolt;
import com.oblivioussp.spartanweaponry.item.ItemCrossbow;
import com.oblivioussp.spartanweaponry.item.ItemLongbow;
import com.oblivioussp.spartanweaponry.util.NBTHelper;
import com.oblivioussp.spartanweaponry.util.Quaternion;
import eaglemixins.EagleMixins;
import eaglemixins.compat.SpartanWeaponryUtil;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.mixin.spartanweaponry.IItemCrossbow_InvokerMixin;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractEntitySkeleton_SpartanBowMixin extends EntityMob {

    @Unique
    private static final String UUID_WEAPON_RANGE = "e7b2eccc-c495-42d9-81e8-9593f74be7f1";
    @Unique
    private static final String WEAPON_RANGE_MODIFIER = EagleMixins.MODID + ":spartanWeaponRange";
    @Unique
    private AttributeModifier eagleMixins$spartanWeaponRange;

    @Shadow @Final private EntityAIAttackRangedBow<AbstractSkeleton> aiArrowAttack;

    @Shadow protected abstract EntityArrow getArrow(float distanceFactor);

    public AbstractEntitySkeleton_SpartanBowMixin(World world) {
        super(world);
    }

    @Inject(
            method = "setCombatTask",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/EntityAITasks;removeTask(Lnet/minecraft/entity/ai/EntityAIBase;)V", ordinal = 0)
    )
    private void eagleMixins_vanillaAbstractSkeleton_setCombatTaskRemoveRangeBonus(CallbackInfo ci) {
        if(this.eagleMixins$spartanWeaponRange != null && ForgeConfigHandler.mobequipment.spartanSkeletons.enableFollowRangeBonus) {
            IAttributeInstance followRange = this.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE);
            if (followRange.hasModifier(eagleMixins$spartanWeaponRange)) followRange.removeModifier(eagleMixins$spartanWeaponRange);
        }
    }

    @ModifyArg(
            method = "setCombatTask",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/EntityAIAttackRangedBow;setAttackCooldown(I)V")
    )
    private int eagleMixins_vanillaAbstractSkeleton_setCombatTaskModifyForLongbow(int cooldown) {
        ItemStack itemStack = this.getHeldItemMainhand();
        if(itemStack.getItem() instanceof ItemLongbow){
            this.eagleMixins$applyBonusFollowRange(SpartanWeaponryUtil.getMaxVelocity(itemStack));
            return (cooldown / 20) * SpartanWeaponryUtil.getAimAndLoadingTicks(itemStack);
        }
        return cooldown;
    }

    @WrapOperation(
            method = "setCombatTask",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/EntityAITasks;addTask(ILnet/minecraft/entity/ai/EntityAIBase;)V", ordinal = 1)
    )
    private void eagleMixins_vanillaAbstractSkeleton_setCombatTaskCreateForCrossbow(EntityAITasks instance, int priority, EntityAIBase task, Operation<Void> original){
        if(this.getHeldItemMainhand().getItem() instanceof ItemCrossbow){
            ItemStack itemStack = this.getHeldItemMainhand();
            this.eagleMixins$applyBonusFollowRange(SpartanWeaponryUtil.getMaxVelocity(itemStack));

            int cooldown = SpartanWeaponryUtil.getAimAndLoadingTicks(itemStack);
            if(this.world.getDifficulty() != EnumDifficulty.HARD) cooldown *= 2;
            this.aiArrowAttack.setAttackCooldown(cooldown);

            original.call(instance, priority, this.aiArrowAttack);
        }
        else{
            original.call(instance, priority, task);
        }
    }

    @WrapOperation(
            method = "attackEntityWithRangedAttack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/EntityArrow;shoot(DDDFF)V")
    )
    private void eagleMixins_vanillaAbstractSkeleton_attackEntityWithRangedAttackWithSpartan(EntityArrow instance, double x, double y, double z, float velocity, float inaccuracy, Operation<Void> original, @Local(argsOnly = true) float distanceFactor){
        if(SpartanWeaponryUtil.isHoldingSpartanBow(this)){
            ItemStack itemStack = this.getHeldItemMainhand();
            if (itemStack.getItem() instanceof ItemLongbow){
                instance.shoot(
                        x,
                        y,
                        z,
                        velocity * ((ItemLongbow)itemStack.getItem()).getMaxArrowSpeed(),
                        inaccuracy * 0.5F
                );
            }
            else if (itemStack.getItem() instanceof IItemCrossbow_InvokerMixin) {
                int shots = 1;
                float projectileAngle = 0F;
                NBTTagCompound nbtLoadedBolt = NBTHelper.getTagCompound(itemStack, ItemCrossbow.nbtAmmoStack);
                if (nbtLoadedBolt.hasKey("Count") && nbtLoadedBolt.getInteger("Count") > 1) {
                    shots = 3;
                }

                Vec3d lookVec = this.getLook(1.0F);
                Vec3d vector = new Vec3d(lookVec.x, lookVec.y, lookVec.z);
                for (int i = 0; i < shots; i++) {
                    if (projectileAngle != 0F) {
                        Vec3d shooterUpVec = ((IItemCrossbow_InvokerMixin) itemStack.getItem()).invokeCalculateEntityViewVector(
                                this.rotationPitch - 90.0f,
                                this.rotationYaw
                        );
                        Quaternion quat = new Quaternion(shooterUpVec, projectileAngle, true);
                        vector = quat.transformVector(lookVec);
                    }

                    EntityArrow entityBolt = instance;
                    if (projectileAngle != 0F) {
                        entityBolt = this.getArrow(distanceFactor);
                    }
                    entityBolt.shoot(
                            vector.x,
                            vector.y,
                            vector.z,
                            velocity * ((ItemCrossbow) itemStack.getItem()).getBoltSpeed(),
                            0F
                    );

                    if (projectileAngle != 0F) this.world.spawnEntity(entityBolt);
                    projectileAngle = (1 - 2 * i) * 10F;
                }
            }
        }
        else{
            original.call(instance, x, y, z, velocity, inaccuracy);
        }
    }

    @WrapOperation(
            method = "attackEntityWithRangedAttack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/monster/AbstractSkeleton;playSound(Lnet/minecraft/util/SoundEvent;FF)V")
    )
    private void eagleMixins_vanillaAbstractSkeleton_attackEntityWithRangedAttackCrossbowSound(AbstractSkeleton instance, SoundEvent soundIn, float volume, float pitch, Operation<Void> original){
        if(this.getHeldItemMainhand().getItem() instanceof ItemCrossbow)
            soundIn = SoundRegistry.CROSSBOW_FIRE;
        original.call(instance, soundIn, volume, pitch);
    }

    @WrapMethod(
            method = "getArrow"
    )
    private EntityArrow eagleMixins_vanillaAbstractSkeleton_getArrowForSpartan(float distanceFactor, Operation<EntityArrow> original){
        if(SpartanWeaponryUtil.isHoldingSpartanBow(this)){
            ItemStack itemStack = this.getHeldItemMainhand();

            if (itemStack.getItem() instanceof ItemCrossbow){
                ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);

                if (itemstack.getItem() == ItemRegistrySW.boltSpectral) {
                    EntityBoltSpectral entityBoltSpectral = new EntityBoltSpectral(this.world, this);
                    entityBoltSpectral.setEnchantmentEffectsFromEntity(this, (float) (entityBoltSpectral.getDamage() * (distanceFactor / 2F)));
                    return entityBoltSpectral;
                }
                else if (itemstack.getItem() instanceof ItemBolt) {
                    EntityBolt entityBolt = ((ItemBolt) itemstack.getItem()).createBolt(this.world, itemstack, this);
                    entityBolt.setEnchantmentEffectsFromEntity(this, (float) (entityBolt.getDamage() * (distanceFactor / 2F)));
                    return entityBolt;
                }
                else {
                    EntityBoltTipped entityBoltTipped = new EntityBoltTipped(this.world, this);
                    entityBoltTipped.setEnchantmentEffectsFromEntity(this, (float) (entityBoltTipped.getDamage() * (distanceFactor / 2F)));

                    if(itemstack.getItem() == ItemRegistrySW.boltTipped && entityBoltTipped instanceof EntityBoltTipped){
                        entityBoltTipped.setPotionEffect(itemstack);
                    }

                    return entityBoltTipped;
                }
            }
            else if(itemStack.getItem() instanceof ItemLongbow) {
                ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);

                if (itemstack.getItem() == Items.SPECTRAL_ARROW) {
                    EntitySpectralArrow entityspectralarrow = new EntitySpectralArrow(this.world, this);
                    entityspectralarrow.setEnchantmentEffectsFromEntity(this, distanceFactor);
                    return entityspectralarrow;
                } else if (itemstack.getItem() instanceof ItemArrow) {
                    EntityArrow entityArrow = ((ItemArrow) itemstack.getItem()).createArrow(this.world, itemstack, this);
                    entityArrow.setEnchantmentEffectsFromEntity(this, (float) (entityArrow.getDamage() * (distanceFactor / 2F)));
                    return entityArrow;
                } else {
                    EntityArrow entityarrow = original.call(distanceFactor);

                    if (itemstack.getItem() == Items.TIPPED_ARROW && entityarrow instanceof EntityTippedArrow) {
                        ((EntityTippedArrow) entityarrow).setPotionEffect(itemstack);
                    }

                    return entityarrow;
                }
            }
        }
        return original.call(distanceFactor);
    }

    @Unique
    private void eagleMixins$applyBonusFollowRange(double maxVelocityMultiplier){
        if(!ForgeConfigHandler.mobequipment.spartanSkeletons.enableFollowRangeBonus) return;
        if(maxVelocityMultiplier <= 1) return;

        IAttributeInstance followRange = this.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE);
        if(this.eagleMixins$spartanWeaponRange == null){
            this.eagleMixins$spartanWeaponRange = new AttributeModifier(
                    UUID.fromString(UUID_WEAPON_RANGE),
                    WEAPON_RANGE_MODIFIER,
                    maxVelocityMultiplier - 1,
                    1
            );
        }
        if(!followRange.hasModifier(this.eagleMixins$spartanWeaponRange)) followRange.applyModifier(this.eagleMixins$spartanWeaponRange);
    }
}
