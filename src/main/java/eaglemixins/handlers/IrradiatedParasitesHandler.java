package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import nc.capability.radiation.entity.IEntityRads;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
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

        entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(HP_UUID);
        entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).removeModifier(DMG_UUID);
        entity.getEntityAttribute(SharedMonsterAttributes.ARMOR).removeModifier(ARMOR_UUID);

        double hpMod = - Math.min(radsPercent * ForgeConfigHandler.irradiated.hpMultiplier, ForgeConfigHandler.irradiated.hpUpperLimit);
        double dmgMod = - Math.min(radsPercent * ForgeConfigHandler.irradiated.dmgMultiplier, ForgeConfigHandler.irradiated.dmgUpperLimit);
        double armorMod = - Math.min(radsPercent * ForgeConfigHandler.irradiated.armorMultiplier, ForgeConfigHandler.irradiated.armorUpperLimit);

        entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier(HP_UUID, "irradiated hp", hpMod, 2));
        entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).applyModifier(new AttributeModifier(DMG_UUID, "irradiated dmg", dmgMod, 2));
        entity.getEntityAttribute(SharedMonsterAttributes.ARMOR).applyModifier(new AttributeModifier(ARMOR_UUID, "irradiated armor", armorMod, 2));
    }
}
