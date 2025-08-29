package eaglemixins.potion;


import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;

import javax.annotation.Nonnull;

public class PotionRadiationFatigue extends PotionBase {

    public static final PotionRadiationFatigue INSTANCE = new PotionRadiationFatigue();
    public PotionRadiationFatigue() {super("radiation_fatigue", true, 0x9BA132);}

    @Override
    public boolean shouldRender(@Nonnull PotionEffect effect) { return false; }
    @Override
    public boolean shouldRenderHUD(@Nonnull PotionEffect effect) { return false; }
    @Override
    public boolean shouldRenderInvText(@Nonnull PotionEffect effect) { return false; }

    @Override
    public void performEffect(@Nonnull EntityLivingBase entity, int amplifier) {
        // No need to do anything here
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false; // Don't tick — handled in event instead
    }
}