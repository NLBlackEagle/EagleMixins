package eaglemixins.potion;


import eaglemixins.util.RadiationDamageSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;

import javax.annotation.Nonnull;


public class PotionRadiationSickness extends PotionBase {

    public static final PotionRadiationSickness INSTANCE = new PotionRadiationSickness();

    //Color is overridden by rad_weakness
    public PotionRadiationSickness() { super("radiation_sickness", true, 0x9BA132); }

    @Override
    public boolean isReady(int duration, int amplifier) { return true; }

    @Override
    public boolean shouldRender(@Nonnull PotionEffect effect) {
        return true;
    }

    @Override
    public boolean shouldRenderHUD(@Nonnull PotionEffect effect) {
        return true;
    }

    @Override
    public boolean shouldRenderInvText(@Nonnull PotionEffect effect) {
        return true;
    }

    @Override
    public void performEffect(EntityLivingBase entityLivingBase, int amplifier) {
        if (entityLivingBase.world.isRemote) return;
        if (!(entityLivingBase instanceof EntityPlayer)) { return;}

        float currentHealth = entityLivingBase.getHealth();
        float maxHealth = entityLivingBase.getMaxHealth();

        switch (amplifier) {

            case 0:
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationWeakness.INSTANCE, 40, 0));
                break;
            case 1:
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationWeakness.INSTANCE, 40, 0));
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationFatigue.INSTANCE, 40, 0));
                break;
            case 2:
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationWeakness.INSTANCE, 40, amplifier - 1));
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationFatigue.INSTANCE, 40, amplifier - 1));
                ((EntityPlayer) entityLivingBase).addExhaustion(0.05F * (float) (amplifier + 1));

                if (currentHealth > 0.75f * maxHealth) {
                    entityLivingBase.attackEntityFrom(RadiationDamageSource.RADIATION, 1.0F);
                }

                break;
            case 3:
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationWeakness.INSTANCE, 40, amplifier - 1));
                entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationFatigue.INSTANCE, 40, amplifier - 1));
                ((EntityPlayer) entityLivingBase).addExhaustion(0.05F * (float) (amplifier + 1));

                if (currentHealth > 0.5f * maxHealth) {
                    entityLivingBase.attackEntityFrom(RadiationDamageSource.RADIATION, 1.0F);
                }

                break;

            default:
                if (amplifier >= 4) {
                    entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationWeakness.INSTANCE, 40, amplifier - 1));
                    entityLivingBase.addPotionEffect(new PotionEffect(PotionRadiationFatigue.INSTANCE, 40, amplifier - 1));
                    ((EntityPlayer) entityLivingBase).addExhaustion(0.05F * (float) (amplifier + 1));
                    entityLivingBase.attackEntityFrom(RadiationDamageSource.RADIATION, 1.0F);

                }
                break;
        }
    }
}