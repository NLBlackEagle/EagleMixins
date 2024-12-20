package eaglemixins.handlers;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import eaglemixins.EagleMixins;


@Mod.EventBusSubscriber(modid = EagleMixins.MODID)
public class ModRegistry {

        public static BerianHandler berianHandler = null;

        public static void init() {
                berianHandler = new BerianHandler();
                MinecraftForge.EVENT_BUS.register(berianHandler);
        }

}