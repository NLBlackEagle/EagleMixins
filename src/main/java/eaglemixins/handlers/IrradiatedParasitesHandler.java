package eaglemixins.handlers;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import eaglemixins.config.ForgeConfigHandler;
import nc.capability.radiation.source.IRadiationSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

public class IrradiatedParasitesHandler {
    private static final UUID HP_UUID = UUID.fromString("todo");
    private static final UUID DMG_UUID = UUID.fromString("todo");
    private static final UUID ARMOR_UUID = UUID.fromString("todo");

    @SubscribeEvent
    private static void onEntityTick(LivingEvent.LivingUpdateEvent event){
        EntityLivingBase entity = event.getEntityLiving();
        if(!(entity instanceof EntityParasiteBase)) return;
        World world = entity.world;
        if(world.isRemote) return;
        if(world.getTotalWorldTime() % 100 != 57) return;

        IRadiationSource chunkRadiationCap = world.getChunk(entity.getPosition()).getCapability(IRadiationSource.CAPABILITY_RADIATION_SOURCE, null);
        if(chunkRadiationCap == null) return;
        double radiationLevel = chunkRadiationCap.getRadiationLevel();

        /*IEntityRads entityRadiation = entity.getCapability(IEntityRads.CAPABILITY_ENTITY_RADS, null);
        if(entityRadiation == null) return;
        double rads = entityRadiation.getRadsPercentage();*/ //TODO: use this instead of the above chunk code if the entity rads is supposed to be used instead of the chunk radiation level

        entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(HP_UUID);
        entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).removeModifier(DMG_UUID);
        entity.getEntityAttribute(SharedMonsterAttributes.ARMOR).removeModifier(ARMOR_UUID);

        double hpMod = - Math.min(radiationLevel * ForgeConfigHandler.irradiated.hpMultiplier, ForgeConfigHandler.irradiated.hpUpperLimit);
        double dmgMod = - Math.min(radiationLevel * ForgeConfigHandler.irradiated.dmgMultiplier, ForgeConfigHandler.irradiated.dmgUpperLimit);
        double armorMod = - Math.min(radiationLevel * ForgeConfigHandler.irradiated.armorMultiplier, ForgeConfigHandler.irradiated.armorUpperLimit);

        entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier(HP_UUID, "irradiated hp", hpMod, 2));
        entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).applyModifier(new AttributeModifier(DMG_UUID, "irradiated dmg", dmgMod, 2));
        entity.getEntityAttribute(SharedMonsterAttributes.ARMOR).applyModifier(new AttributeModifier(ARMOR_UUID, "irradiated armor", armorMod, 2));
    }
}
