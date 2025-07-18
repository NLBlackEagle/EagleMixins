package eaglemixins.handlers;

import eaglemixins.EagleMixins;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = EagleMixins.MODID)
public class ModRegistry {

    @SubscribeEvent
    public static void registerPotionEvent(RegistryEvent.Register<Potion> event) {
        event.getRegistry().register(eaglemixins.potion.PotionRadiationSickness.INSTANCE);
        event.getRegistry().register(eaglemixins.potion.PotionRadiationFatigue.INSTANCE);
        event.getRegistry().register(eaglemixins.potion.PotionRadiationWeakness.INSTANCE);
        event.getRegistry().register(eaglemixins.potion.PotionTeleportationSickness.INSTANCE);
    }
}

