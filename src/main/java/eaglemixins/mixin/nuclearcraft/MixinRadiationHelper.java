package eaglemixins.mixin.nuclearcraft;

import eaglemixins.attribute.ModAttributes;
import nc.capability.radiation.entity.IEntityRads;
import nc.radiation.RadiationHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RadiationHelper.class)
public class MixinRadiationHelper {

    @ModifyVariable(
            method = "addRadsToEntity",
            at = @At("STORE"),
            name = "addedRadiation",
            remap = false
    )
    private static double eaglemixins_ncRadiationHelper_applyRadResAttribute(double originalAddedRadiationAfterResistance, IEntityRads entityRads, EntityLivingBase entity){
        if (!(entity instanceof EntityPlayer) || entity.world.isRemote) return originalAddedRadiationAfterResistance;
        if (originalAddedRadiationAfterResistance <= 0.0) return originalAddedRadiationAfterResistance;

        IAttributeInstance inst = entity.getEntityAttribute(ModAttributes.RADIATION_RESISTANCE);
        if (inst == null) return originalAddedRadiationAfterResistance;

        double perTickCap = inst.getAttributeValue();
        if (perTickCap <= 0.0 || Double.isNaN(perTickCap) || Double.isInfinite(perTickCap)) return originalAddedRadiationAfterResistance; //this will never happen but whatever

        return Math.max(0.0, originalAddedRadiationAfterResistance - perTickCap);
    }
}
