package eaglemixins.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class PotionTeleportationSickness extends PotionBase {

    public static final PotionTeleportationSickness INSTANCE = new PotionTeleportationSickness();

    public PotionTeleportationSickness() {super("teleportation_sickness", true, 0xF3F4F9);}

    @Override
    public void performEffect(EntityLivingBase entityLivingBase, int amplifier) {
        if (entityLivingBase.world.isRemote) return;
        if (entityLivingBase.isPotionActive(MobEffects.NAUSEA)) return;

        PotionEffect thisEffect = entityLivingBase.getActivePotionEffect(this);
        if (thisEffect != null) {
            int duration = thisEffect.getDuration();
            entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, duration, amplifier));
        }
    }

    @Override
    public boolean shouldRender(PotionEffect effect) {
        return true;
    }

    @Override
    public boolean shouldRenderHUD(PotionEffect effect) {
        return true;
    }

    @Override
    public boolean shouldRenderInvText(PotionEffect effect) {
        return true;
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }
}