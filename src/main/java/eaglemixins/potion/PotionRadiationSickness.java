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
        EntityPlayer player = (EntityPlayer) entityLivingBase;

        if(player.world.getWorldTime() % 20 == 0) {
            player.addPotionEffect(new PotionEffect(PotionRadiationWeakness.INSTANCE, 40, Math.max(0, amplifier - 1)));
            if(amplifier > 0) player.addPotionEffect(new PotionEffect(PotionRadiationFatigue.INSTANCE, 40, amplifier - 1));
        }
        if(amplifier > 1)
            player.addExhaustion(0.05F * (float) (amplifier + 1));

        float healthPerc = player.getHealth() / player.getMaxHealth();

        switch (amplifier) {
            case 0: case 1: break;
            case 2: if (healthPerc > 0.75F) player.attackEntityFrom(RadiationDamageSource.RADIATION, 1.0F); break;
            case 3: if (healthPerc > 0.5F) player.attackEntityFrom(RadiationDamageSource.RADIATION, 1.0F); break;
            default: player.attackEntityFrom(RadiationDamageSource.RADIATION, 1.0F); break;
        }
    }
}