package eaglemixins.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public class PotionRadiationSickness extends PotionBase {

    public static final PotionRadiationSickness INSTANCE = new PotionRadiationSickness();
    public PotionRadiationSickness() { super("radiation_sickness", true, 0xF3F4F9); }


    @Override
    public boolean isReady(int duration, int amplifier) { return true; }


    @Override
    public void performEffect(EntityLivingBase entityLivingBase, int amplifier) {
        if(entityLivingBase.world.isRemote) return;
        if(entityLivingBase.getActivePotionEffect(this) != null) return;

        switch(entityLivingBase.getActivePotionEffect(this).getAmplifier()) {
            case 0:
            case 1:
            case 2: if (entityLivingBase instanceof EntityPlayer) { ((EntityPlayer) entityLivingBase).addExhaustion(0.005F * (float) (amplifier + 1)); }
            case 3: if (entityLivingBase.getHealth() > 1.0F) { entityLivingBase.attackEntityFrom(DamageSource.MAGIC, 1.0F);}
            case 4: entityLivingBase.attackEntityFrom(DamageSource.WITHER, 1.0F); //entityLivingBase.attackEntityFrom(new DamageSource("Radiation"), 1.0F);
        }
    }
}