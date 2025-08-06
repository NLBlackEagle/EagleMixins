package eaglemixins;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.handlers.*;
import eaglemixins.network.PacketStartTeleportOverlay;
import eaglemixins.network.PacketStopTeleportOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = EagleMixins.MODID, version = EagleMixins.VERSION, name = EagleMixins.NAME, dependencies = "required-after:fermiumbooter")
public class EagleMixins {

    public static final String MODID = "eaglemixins";
    public static final String VERSION = "1.1.3";
    public static final String NAME = "EagleMixins";
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);


    @Mod.Instance(value = MODID)
    public static EagleMixins INSTANCE;

	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(BlockNoclipHandler.class);
        MinecraftForge.EVENT_BUS.register(NuclearCraftInteractions.class);
        MinecraftForge.EVENT_BUS.register(AbyssalRiftHandler.class);
        MinecraftForge.EVENT_BUS.register(BarrierBlockHandler.class);
        MinecraftForge.EVENT_BUS.register(BerianHandler.class);
        MinecraftForge.EVENT_BUS.register(BerryDebuffHandler.class);
        MinecraftForge.EVENT_BUS.register(BlightedShivaxiHandler.class);
        MinecraftForge.EVENT_BUS.register(BlockDropsHandler.class);
        MinecraftForge.EVENT_BUS.register(ConductivityHandler.class);
        MinecraftForge.EVENT_BUS.register(DamageFalloffHandler.class);
        MinecraftForge.EVENT_BUS.register(DismountHandler.class);
        MinecraftForge.EVENT_BUS.register(DispelEntityHandler.class);
        MinecraftForge.EVENT_BUS.register(EntityRenameCancelHandler.class);
        MinecraftForge.EVENT_BUS.register(FURHandler.class);
        MinecraftForge.EVENT_BUS.register(HealthValidationHandler.class);
        MinecraftForge.EVENT_BUS.register(PotionEffectsByFluidsHandler.class);
        MinecraftForge.EVENT_BUS.register(RandomTippedArrowHandler.class);
        MinecraftForge.EVENT_BUS.register(RandomTpCancelHandler.class);
        MinecraftForge.EVENT_BUS.register(RecallFlightCancelHandler.class);
        MinecraftForge.EVENT_BUS.register(SentientWeaponEvolutionHandler.class);
        MinecraftForge.EVENT_BUS.register(SRParasitesHandler.class);
        MinecraftForge.EVENT_BUS.register(BlockBreakSlowHandler.class);
        MinecraftForge.EVENT_BUS.register(AbyssalGateHandler.class);
        if(ForgeConfigHandler.irradiated.enabled) MinecraftForge.EVENT_BUS.register(IrradiatedParasitesHandler.class);
        MinecraftForge.EVENT_BUS.register(TileCounterHandler.class);
        MinecraftForge.EVENT_BUS.register(EntitySpawnListener.class);
        ForgeConfigHandler.refreshDrinkableBlockCache();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        EnhancedVisualsHandler.init();
        EntitySpawnListener.init();
        ModStats.init();


        NETWORK.registerMessage(
                PacketStartTeleportOverlay.Handler.class,
                PacketStartTeleportOverlay.class,
                0,
                Side.CLIENT
        );

        NETWORK.registerMessage(
                PacketStopTeleportOverlay.Handler.class,
                PacketStopTeleportOverlay.class,
                1, // Next available ID (0 is already used)
                Side.CLIENT
        );
    }
}

