package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.potion.PotionRadiationSickness;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

/** Players with Radiation Sickness can't receive the configured potion effects. Already active ones are left to run out. */
public class RadiationPotionBlockHandler {

    //Deny blocked effects from being applied while sick
    @SubscribeEvent
    public static void onPotionApplicable(PotionEvent.PotionApplicableEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        if (!event.getEntityLiving().isPotionActive(PotionRadiationSickness.INSTANCE)) return;
        if (getBlockedPotions().contains(event.getPotionEffect().getPotion()))
            event.setResult(Event.Result.DENY);
    }

    private static Set<Potion> blockedPotions = null;

    private static Set<Potion> getBlockedPotions() {
        if (blockedPotions == null) {
            blockedPotions = new HashSet<>();
            for (String potionString : ForgeConfigHandler.nuclear.radiationBlockedPotions) {
                ResourceLocation location = new ResourceLocation(potionString.trim());
                if (ForgeRegistries.POTIONS.containsKey(location))
                    blockedPotions.add(ForgeRegistries.POTIONS.getValue(location));
            }
        }
        return blockedPotions;
    }

    public static void refreshConfig() {
        blockedPotions = null;
    }
}
