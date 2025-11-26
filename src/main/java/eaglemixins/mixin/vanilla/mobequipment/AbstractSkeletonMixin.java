package eaglemixins.mixin.vanilla.mobequipment;

import eaglemixins.config.folders.MobEquipmentConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin extends EntityLivingBase {
    public AbstractSkeletonMixin(World worldIn) {
        super(worldIn);
    }

    @ModifyArg(
            method = "setEquipmentBasedOnDifficulty",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;<init>(Lnet/minecraft/item/Item;)V")
    )
    private Item eaglemixins_vanillaAbstractSkeleton_setEquipmentBasedOnDifficulty(Item itemIn){
        return MobEquipmentConfig.getRandomItem(this.getRNG(), false);
    }
}
