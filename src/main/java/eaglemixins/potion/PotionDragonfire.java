package eaglemixins.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;

public class PotionDragonfire extends PotionBase {

    public static final PotionDragonfire INSTANCE = new PotionDragonfire();
    public static final int SECONDS_PER_LEVEL = 16;

    public PotionDragonfire() {super("dragonfire", true, 0xFF5A00);}

    // Keeps the entity burning for as long as the effect lasts, re-igniting it if extinguished
    @Override
    public void performEffect(EntityLivingBase entityLivingBase, int amplifier) {
        if (entityLivingBase.world.isRemote) return;
        if (entityLivingBase.isImmuneToFire() || entityLivingBase.isBurning()) return;

        PotionEffect thisEffect = entityLivingBase.getActivePotionEffect(this);
        if (thisEffect != null) {
            entityLivingBase.setFire(Math.max(1, (thisEffect.getDuration() + 19) / 20));
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 5 == 0;
    }

    public static PotionEffect create(int level) {
        int amplifier = Math.max(0, level - 1);
        return new PotionEffect(INSTANCE, SECONDS_PER_LEVEL * 20 * (amplifier + 1), amplifier);
    }
}
