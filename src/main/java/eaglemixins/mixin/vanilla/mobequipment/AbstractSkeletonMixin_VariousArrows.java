package eaglemixins.mixin.vanilla.mobequipment;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin_VariousArrows extends EntityLivingBase {
    public AbstractSkeletonMixin_VariousArrows(World worldIn) {
        super(worldIn);
    }

    @WrapMethod(method = "getArrow")
    private EntityArrow eagleMixins_vanillaAbstractSkeleton_getArrowEntityFromOffhandItem(float distanceFactor, Operation<EntityArrow> original){
        ItemStack offhandStack = this.getHeldItemOffhand();

        if (offhandStack.getItem() instanceof ItemArrow) {
            EntityArrow entityArrow = ((ItemArrow) offhandStack.getItem()).createArrow(this.world, offhandStack, this);
            entityArrow.setEnchantmentEffectsFromEntity(this, (float) (entityArrow.getDamage() / 2. * (double) distanceFactor));

            if (offhandStack.getItem() == Items.TIPPED_ARROW && entityArrow instanceof EntityTippedArrow)
                ((EntityTippedArrow) entityArrow).setPotionEffect(offhandStack);

            return entityArrow;
        } else {
            //No arrow in offhand -> use default entity behavior (wither shoots flamed, stray shoots slowness tipped, forsaken shoots fur:fragile tipped)
            return original.call(distanceFactor);
        }
    }
}
