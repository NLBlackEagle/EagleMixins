package eaglemixins.potion;


import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;

public class PotionRadiationFatigue extends PotionBase {

    public static final PotionRadiationFatigue INSTANCE = new PotionRadiationFatigue();
    public PotionRadiationFatigue() {super("radiation_fatigue", true, 0xF3F4F9);}

    @Override
    public boolean shouldRender(PotionEffect effect) { return false; }
    @Override
    public boolean shouldRenderHUD(PotionEffect effect) { return false; }
    @Override
    public boolean shouldRenderInvText(PotionEffect effect) { return false; }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        // No need to do anything here
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false; // Don't tick — handled in event instead
    }
}