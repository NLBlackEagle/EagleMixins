package eaglemixins.handlers;

import bettercombat.mod.event.RLCombatSweepEvent;
import com.Fishmod.mod_LavaCow.init.FishItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FURHandler {

    @SubscribeEvent
    public static void onScytheSweep(RLCombatSweepEvent event) {
        if(event.getItemStack().getItem() == FishItems.REAPERS_SCYTHE) {
            EntityPlayer attacker = event.getEntityPlayer();
            event.setDoSweep(true);
            event.setSweepingAABB(event.getSweepingAABB().grow(1.0F, 0, 1.0F));
            attacker.world.playSound((EntityPlayer)null, attacker.posX, attacker.posY, attacker.posZ, FishItems.ENTITY_SCARECROW_SCYTHE, attacker.getSoundCategory(), 1.0F, 1.0F / (attacker.world.rand.nextFloat() * 0.4F + 0.8F));
        }
    }
}
