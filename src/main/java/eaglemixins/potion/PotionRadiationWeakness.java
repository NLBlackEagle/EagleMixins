package eaglemixins.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.potion.PotionEffect;
import java.util.UUID;


public class PotionRadiationWeakness extends PotionBase {

    private static final UUID WEAKNESS_MODIFIER_UUID = UUID.fromString("3c3a8cd3-2bc2-4ad9-9a9a-3d25b7b2a5f3");
    public static final PotionRadiationWeakness INSTANCE = new PotionRadiationWeakness();
    public PotionRadiationWeakness() {super("radiation_weakness", true, 0x9BA132);}

    @Override
    public boolean shouldRender(PotionEffect effect) { return false; }
    @Override
    public boolean shouldRenderHUD(PotionEffect effect) { return false; }
    @Override
    public boolean shouldRenderInvText(PotionEffect effect) { return false; }


    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        IAttributeInstance attr = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (attr == null) return;

        // Remove previous modifier if it exists
        if (attr.getModifier(WEAKNESS_MODIFIER_UUID) != null) {
            attr.removeModifier(WEAKNESS_MODIFIER_UUID);
        }

        // Apply new modifier
        double amount = -2.0D * (amplifier + 1); // Adjust as needed
        AttributeModifier modifier = new AttributeModifier(WEAKNESS_MODIFIER_UUID, "hidden_weakness", amount, 0);
        attr.applyModifier(modifier);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}