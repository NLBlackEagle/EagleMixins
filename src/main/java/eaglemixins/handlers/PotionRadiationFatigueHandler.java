package eaglemixins.handlers;

import eaglemixins.potion.PotionRadiationFatigue;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PotionRadiationFatigueHandler {

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