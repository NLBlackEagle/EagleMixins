package eaglemixins.handlers;

import eaglemixins.EagleMixins;
import nc.capability.radiation.entity.IEntityRads;
import nc.radiation.RadiationHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = EagleMixins.MODID)
public final class RadiationResistanceApplier {

    private RadiationResistanceApplier() {}

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;
        if (!(event.getEntity() instanceof EntityLivingBase)) return;

        EntityLivingBase living = (EntityLivingBase) event.getEntity();
        ResourceLocation id = EntityList.getKey(living);
        if (id == null) return;

        double resistance = eaglemixins.registry.RadiationResistanceRegistry.get(id);
        if (resistance <= 0.0D) return;

        IEntityRads rads = RadiationHelper.getEntityRadiation(living);
        if (rads == null) return;

        // Try common setter names across NC 1.12.2 variants.
        boolean applied = trySet(rads, "setRadiationResistance", resistance)
                || trySet(rads, "setInternalRadiationResistance", resistance)
                || trySet(rads, "setExternalRadiationResistance", resistance);

        if (applied) {
            EagleMixins.LOGGER.debug("[EagleMixins] Applied resistance {} to {}", resistance, id);
        } else {
            EagleMixins.LOGGER.warn("[EagleMixins] RadRes No compatible setter on IEntityRads for {} (tried setRadiationResistance/internal/external)", id);
        }
    }

    private static boolean trySet(IEntityRads rads, String method, double value) {
        try {
            rads.getClass().getMethod(method, double.class).invoke(rads, value);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}
