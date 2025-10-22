package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import nc.capability.radiation.entity.IEntityRads;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

public class IrradiatedParasitesHandler {
    private static final UUID HP_UUID = UUID.fromString("88b143a9-2850-4415-9dfc-810159c5179f");
    private static final UUID DMG_UUID = UUID.fromString("e6c9d15c-7835-4eb0-ad7e-a41c90a5a483");
    private static final UUID ARMOR_UUID = UUID.fromString("0cdaa41f-92d6-4575-9acc-3527810a859e");

    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingUpdateEvent event){
        EntityLivingBase entity = event.getEntityLiving();

        ResourceLocation loc = EntityList.getKey(entity);
        if(loc == null) return;
        String entityId = loc.toString();
        String modId = loc.getNamespace() + ":*";
        boolean isInWhitelist =
                ForgeConfigHandler.irradiated.getIrradiatedEntityList().contains(entityId) ||
                ForgeConfigHandler.irradiated.getIrradiatedEntityList().contains(modId);
        if(isInWhitelist == ForgeConfigHandler.irradiated.irradiatedEntitiesIsBlacklist) return;

        World world = entity.world;
        if(world.isRemote) return;
        if(entity.ticksExisted % 100 != 57) return;

        IEntityRads entityRadiation = entity.getCapability(IEntityRads.CAPABILITY_ENTITY_RADS, null);
        if(entityRadiation == null) return;
        double radsPercent = entityRadiation.getRadsPercentage();

        applyModifier(entity, radsPercent, SharedMonsterAttributes.MAX_HEALTH, HP_UUID, ForgeConfigHandler.irradiated.hpMultiplier, ForgeConfigHandler.irradiated.hpUpperLimit);
        applyModifier(entity, radsPercent, SharedMonsterAttributes.ATTACK_DAMAGE, DMG_UUID, ForgeConfigHandler.irradiated.dmgMultiplier, ForgeConfigHandler.irradiated.dmgUpperLimit);
        applyModifier(entity, radsPercent, SharedMonsterAttributes.ARMOR, ARMOR_UUID, ForgeConfigHandler.irradiated.armorMultiplier, ForgeConfigHandler.irradiated.armorUpperLimit);
    }

    private static void applyModifier(EntityLivingBase entity, double radsPercent, IAttribute attribute, UUID uuid, double multi, double cap){
        if (entity.getEntityAttribute(attribute) == null)
            return;
        entity.getEntityAttribute(attribute).removeModifier(uuid);
        double modifierAmount = - Math.min(radsPercent * multi, cap);
        if(Math.abs(modifierAmount) > 1e-3)
            entity.getEntityAttribute(attribute).applyModifier(new AttributeModifier(uuid, "irradiated", modifierAmount, 2));
    }
}
