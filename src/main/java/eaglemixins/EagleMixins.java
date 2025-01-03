package eaglemixins;

import eaglemixins.handlers.BarrierBlockHandler;
import eaglemixins.handlers.HealthValidationHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import eaglemixins.handlers.BerianHandler;

@Mod(modid = EagleMixins.MODID, version = EagleMixins.VERSION, name = EagleMixins.NAME, dependencies = "required-after:fermiumbooter")
public class EagleMixins {

    public static final String MODID = "eaglemixins";
    public static final String VERSION = "1.0.3";
    public static final String NAME = "EagleMixins";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(BerianHandler.class);
        MinecraftForge.EVENT_BUS.register(HealthValidationHandler.class);
        MinecraftForge.EVENT_BUS.register(BarrierBlockHandler.class);
    }
}