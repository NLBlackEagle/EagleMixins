package eaglemixins.potion;

import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;

public class PotionWildfire extends PotionBase {

    public static final PotionWildfire INSTANCE = new PotionWildfire();

    public PotionWildfire() {super("wildfire", true, 0xFF5A00);}

    // Keeps the entity burning for as long as the effect lasts, re-igniting it once it leaves water/rain
    @Override
    public void performEffect(EntityLivingBase entityLivingBase, int amplifier) {
        if (entityLivingBase.world.isRemote) return;
        if (!ForgeConfigHandler.wildfire.reignite) return;
        if (entityLivingBase.isImmuneToFire() || entityLivingBase.isBurning()) return;
        if (entityLivingBase.isWet()) return;

        PotionEffect thisEffect = entityLivingBase.getActivePotionEffect(this);
        if (thisEffect != null) {
            entityLivingBase.setFire(Math.max(1, (thisEffect.getDuration() + 19) / 20));
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 5 == 0;
    }

    public static int getFireSeconds(int level) {
        return ForgeConfigHandler.wildfire.secondsPerLevel * Math.max(1, level);
    }

    public static PotionEffect create(int level) {
        int amplifier = Math.max(0, level - 1);
        return new PotionEffect(INSTANCE, getFireSeconds(level) * 20, amplifier);
    }
}
