package eaglemixins.potion;

import net.minecraft.entity.SharedMonsterAttributes;

import javax.annotation.Nonnull;
import java.util.UUID;

public class PotionRadiationWeakness extends PotionBase {
    private static final UUID WEAKNESS_MODIFIER_UUID =
            UUID.fromString("3c3a8cd3-2bc2-4ad9-9a9a-3d25b7b2a5f3");

    public static final PotionRadiationWeakness INSTANCE = new PotionRadiationWeakness();

    public PotionRadiationWeakness() {
        super("radiation_weakness", true, 0x9BA132);

        this.registerPotionAttributeModifier(SharedMonsterAttributes.ATTACK_DAMAGE, WEAKNESS_MODIFIER_UUID.toString(), -2.0D, 0);
    }

    @Override public boolean shouldRender(@Nonnull net.minecraft.potion.PotionEffect e){ return false; }
    @Override public boolean shouldRenderHUD(@Nonnull net.minecraft.potion.PotionEffect e){ return false; }
    @Override public boolean shouldRenderInvText(@Nonnull net.minecraft.potion.PotionEffect e){ return false; }

}