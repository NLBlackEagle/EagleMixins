package eaglemixins.mixin.nuclearcraft;

import eaglemixins.attribute.ModAttributes;
import nc.capability.radiation.entity.IEntityRads;
import nc.radiation.RadiationHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@Mixin(RadiationHelper.class)
public class MixinRadiationHelper {


    @Unique private static final boolean APPLY_WHEN_IGNORE_RESISTANCE = false;
    @Unique private static final Map<UUID, Integer> RAD_LAST_TICK = new WeakHashMap<>();
    @Unique private static final Map<UUID, Double> RAD_REMAIN    = new WeakHashMap<>();

    @Inject(method = "addRadsToEntity", at = @At("RETURN"), cancellable = true, remap = false)
    private static void eaglemixins$directAttrPerTickCap(IEntityRads entityRads, EntityLivingBase entity, double rawRadiation, boolean ignoreResistance, boolean ignoreMultipliers, int updateRate, CallbackInfoReturnable<Double> cir) {

        if (!(entity instanceof EntityPlayer) || entity.world.isRemote) return;

        if (!APPLY_WHEN_IGNORE_RESISTANCE && ignoreResistance) return;

        IAttributeInstance inst = entity.getEntityAttribute(ModAttributes.RADIATION_RESISTANCE);

        if (inst == null) return;

        double perTickCap = inst.getAttributeValue();
        if (!(perTickCap > 0.0)) return;
        if (Double.isNaN(perTickCap) || Double.isInfinite(perTickCap)) return;

        double result = cir.getReturnValue();
        if (!(result > 0.0)) return;

        UUID id = entity.getUniqueID();
        int tick = entity.ticksExisted;

        Integer lastTick = RAD_LAST_TICK.get(id);
        if (lastTick == null || lastTick != tick) {
            RAD_LAST_TICK.put(id, tick);
            RAD_REMAIN.put(id, perTickCap);
        }

        double remain = RAD_REMAIN.getOrDefault(id, 0.0);
        if (remain <= 0.0) return;

        double subtract = Math.min(remain, result);
        double adjusted = result - subtract;

        RAD_REMAIN.put(id, remain - subtract);

        if (adjusted < 0.0) adjusted = 0.0;
        cir.setReturnValue(adjusted);
    }
}
