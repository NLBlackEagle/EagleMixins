package eaglemixins.handlers;

import eaglemixins.potion.PotionRadiationWeakness;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

public class RadiationAttributeHandler {

    private static final UUID WEAKNESS_MODIFIER_UUID = UUID.fromString("3c3a8cd3-2bc2-4ad9-9a9a-3d25b7b2a5f3");

    @SubscribeEvent
    public static void onLivingUpdate(LivingUpdateEvent event) {
        if (!event.getEntityLiving().isPotionActive(PotionRadiationWeakness.INSTANCE)) {
            IAttributeInstance attr = event.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
            if (attr != null && attr.getModifier(WEAKNESS_MODIFIER_UUID) != null) {
                attr.removeModifier(WEAKNESS_MODIFIER_UUID);
            }
        }
    }
}