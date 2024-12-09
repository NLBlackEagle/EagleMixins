package eaglemixins;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import eaglemixins.handlers.ModRegistry;
import eaglemixins.proxy.CommonProxy;

@Mod(modid = EagleMixins.MODID, version = EagleMixins.VERSION, name = EagleMixins.NAME, dependencies = "required-after:fermiumbooter")
public class EagleMixins {
    public static final String MODID = "eaglemixins";
    public static final String VERSION = "EagleMixins.Mod.Version";
    public static final String NAME = "EagleMixins";
    public static final Logger LOGGER = LogManager.getLogger();
	
    @SidedProxy(clientSide = "eaglemixins.proxy.ClientProxy", serverSide = "eaglemixins.proxy.CommonProxy")
    public static CommonProxy PROXY;
	
	@Instance(MODID)
	public static EagleMixins instance;
	
	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModRegistry.init();
        EagleMixins.PROXY.preInit();
    }
}