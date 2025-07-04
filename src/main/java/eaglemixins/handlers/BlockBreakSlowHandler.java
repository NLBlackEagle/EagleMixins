package eaglemixins.handlers;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraft.potion.PotionEffect;
import net.minecraft.entity.player.EntityPlayer;
import eaglemixins.potion.PotionRadiationFatigue;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber

public class BlockBreakSlowHandler {

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null) return;

        PotionEffect effect = player.getActivePotionEffect(PotionRadiationFatigue.INSTANCE);
        if (effect != null) {
            int amplifier = effect.getAmplifier() + 1;

            // Reduce break speed exponentially with higher amplifier
            float originalSpeed = event.getNewSpeed();
            float newSpeed = originalSpeed / (2F * amplifier); // Can adjust scale here

            event.setNewSpeed(newSpeed);
        }
    }
}