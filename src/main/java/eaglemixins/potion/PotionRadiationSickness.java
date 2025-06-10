package eaglemixins.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;

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
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationWeakness.INSTANCE, 40));
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationFatigue.INSTANCE, 40));
                ((EntityPlayer) entityLivingBase).addExhaustion(0.05F * (float) (amplifier + 1));
                break;
            case 3:
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationWeakness.INSTANCE, 40));
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationFatigue.INSTANCE, 40));
                if (entityLivingBase instanceof EntityPlayer) {
                    if (entityLivingBase.getHealth() > 1.0F) {
                        entityLivingBase.attackEntityFrom(DamageSource.MAGIC, 1.0F);
                        break;
                    }
                }
            case 4:
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationWeakness.INSTANCE));
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationFatigue.INSTANCE));
                if (entityLivingBase instanceof EntityPlayer) {
                    entityLivingBase.attackEntityFrom(DamageSource.WITHER, 1.0F);
                    break; //entityLivingBase.attackEntityFrom(new DamageSource("Radiation"), 1.0F);
                }
        }
    }
}