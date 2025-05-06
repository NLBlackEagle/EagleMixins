package eaglemixins.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;


public class PotionRadiationSickness extends PotionBase {

    public static final PotionRadiationSickness INSTANCE = new PotionRadiationSickness();

    public PotionRadiationSickness() { super("radiation_sickness", true, 0xF3F4F9); }

    @Override
    public boolean isReady(int duration, int amplifier) { return true; }

    @Override
    public void performEffect(EntityLivingBase entityLivingBase, int amplifier) {
        if(entityLivingBase.world.isRemote) return;
        if(entityLivingBase.ticksExisted%40==0 && entityLivingBase.world.rand.nextFloat() < 0.30F) {
            entityLivingBase.attackEntityFrom(new DamageSource("Radiation"), 1.0F);
        }
    }
}