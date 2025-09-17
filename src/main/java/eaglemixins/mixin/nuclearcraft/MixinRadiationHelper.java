package eaglemixins.mixin.nuclearcraft;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eaglemixins.attribute.ModAttributes;
import nc.capability.radiation.entity.IEntityRads;
import nc.radiation.RadiationHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RadiationHelper.class)
public class MixinRadiationHelper {

    @ModifyReturnValue(method = "addRadsToEntity", at = @At("RETURN"), remap = false)
    private static double eaglemixins$directAttrPerTickCap(double originalRadiationAddedAfterResistance, IEntityRads entityRads, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer) || entity.world.isRemote) return originalRadiationAddedAfterResistance;
        if (originalRadiationAddedAfterResistance <= 0.0) return originalRadiationAddedAfterResistance;

        IAttributeInstance inst = entity.getEntityAttribute(ModAttributes.RADIATION_RESISTANCE);
        if (inst == null) return originalRadiationAddedAfterResistance;

        double perTickCap = inst.getAttributeValue();
        if (perTickCap <= 0.0 || Double.isNaN(perTickCap) || Double.isInfinite(perTickCap)) return originalRadiationAddedAfterResistance; //this will never happen but whatever

        return Math.max(0.0, originalRadiationAddedAfterResistance - perTickCap);
    }
}
