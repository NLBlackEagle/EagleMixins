package eaglemixins.potion;

import eaglemixins.util.RadiationDamageSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;

public class PotionRadiationSickness extends PotionBase {

    public static final PotionRadiationSickness INSTANCE = new PotionRadiationSickness();
    public PotionRadiationSickness() { super("radiation_sickness", true, 0xF3F4F9); }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    public void performEffect(EntityLivingBase entityLivingBase, int amplifier) {
        if (entityLivingBase.world.isRemote) return;

        switch (amplifier) {
            case 0:
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationWeakness.INSTANCE, 40));
                break;
            case 1:
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationWeakness.INSTANCE, 40));
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationFatigue.INSTANCE, 40));
                break;
            case 2:
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationWeakness.INSTANCE, 40, amplifier - 1));
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationFatigue.INSTANCE, 40, amplifier - 1));
                ((EntityPlayer) entityLivingBase).addExhaustion(0.05F * (float) (amplifier + 1));
                break;
            case 3:
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationWeakness.INSTANCE, 40, amplifier - 1));
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationFatigue.INSTANCE, 40, amplifier - 1));
                if (entityLivingBase instanceof EntityPlayer) {
                    float currentHealth = entityLivingBase.getHealth();
                    float maxHealth = entityLivingBase.getMaxHealth();

                    if (currentHealth > 0.75f * maxHealth) {
                        entityLivingBase.attackEntityFrom(RadiationDamageSource.RADIATION, 1.0F);

                    }
                }
                break;
            case 4:
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationWeakness.INSTANCE));
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationFatigue.INSTANCE));
                if (entityLivingBase instanceof EntityPlayer) {
                    entityLivingBase.attackEntityFrom(RadiationDamageSource.RADIATION, 1.0F);
                }
                break;
        }
    }
}