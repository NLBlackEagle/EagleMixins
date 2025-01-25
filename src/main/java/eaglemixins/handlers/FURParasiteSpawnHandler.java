package eaglemixins.handlers;

import com.Fishmod.mod_LavaCow.entities.EntityParasite;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FURParasiteSpawnHandler {
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntityLiving().world.isRemote) return;
        EntityLivingBase deadEntity = event.getEntityLiving();
        if (deadEntity.world.rand.nextInt(100) >= 10) return;

        ResourceLocation entityId = EntityList.getKey(deadEntity);
        if (entityId == null) return;

        for (String configEntityId : ForgeConfigHandler.server.spawnsFURParasitesOnDeath) {
            if (entityId.toString().equals(configEntityId)) {
                EntityParasite parasite = new EntityParasite(deadEntity.world);
                parasite.setPosition(deadEntity.getPosition().getX(), deadEntity.getPosition().getY(), deadEntity.getPosition().getZ());
                deadEntity.world.spawnEntity(parasite);
                return;
            }
        }
    }
}
