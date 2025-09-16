package eaglemixins;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.handlers.*;
import eaglemixins.network.PacketStartTeleportOverlay;
import eaglemixins.network.PacketStopTeleportOverlay;
import eaglemixins.registry.RadiationResistanceRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
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
    public static final String VERSION = "1.1.4";
    public static final String NAME = "EagleMixins";
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

    @Mod.Instance(value = MODID)
    public static EagleMixins INSTANCE;

	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        registerIfModsPresent(new String[]{"firstaid"}, FirstAidRadiationHandler.class);
        registerIfModsPresent(new String[]{"nuclearcraft"}, ItemRadCapAttacher.class);
        MinecraftForge.EVENT_BUS.register(BlockNoclipHandler.class);
        MinecraftForge.EVENT_BUS.register(NuclearCraftInteractions.class);
        registerIfModsPresent(new String[]{"srparasites", "playerbosses"}, AbyssalRiftHandler.class);
        MinecraftForge.EVENT_BUS.register(BarrierBlockHandler.class);
        MinecraftForge.EVENT_BUS.register(BerianHandler.class);
        registerIfModsPresent(new String[]{"biomesoplenty"}, BerryDebuffHandler.class);
        registerIfModsPresent(new String[]{"champions", "playerbosses"}, BlightedShivaxiHandler.class);
        registerIfModsPresent(new String[]{"biomesoplenty"}, BlockDropsHandler.class);
        MinecraftForge.EVENT_BUS.register(ConductivityHandler.class);
        MinecraftForge.EVENT_BUS.register(DamageFalloffHandler.class);
        MinecraftForge.EVENT_BUS.register(DismountHandler.class);
        MinecraftForge.EVENT_BUS.register(DispelEntityHandler.class);
        registerIfModsPresent(new String[]{"srparasites", "playerbosses"}, EntityRenameCancelHandler.class);
        MinecraftForge.EVENT_BUS.register(FallDamageHandler.class);
        MinecraftForge.EVENT_BUS.register(FallDamageNegation.class);
        registerIfModsPresent(new String[]{"bettercombatmod", "mod_lavacow"}, FURHandler.class);
        registerIfModsPresent(new String[]{"firstaid"}, HealthValidationHandler.class);
        MinecraftForge.EVENT_BUS.register(PotionEffectsByFluidsHandler.class);
        MinecraftForge.EVENT_BUS.register(RadiationAttributeHandler.class);
        registerIfModsPresent(new String[]{"nuclearcraft"},RadiationResistanceApplier.class);
        MinecraftForge.EVENT_BUS.register(RandomTippedArrowHandler.class);
        MinecraftForge.EVENT_BUS.register(RandomTpCancelHandler.class);
        MinecraftForge.EVENT_BUS.register(RecallFlightCancelHandler.class);
        registerIfModsPresent(new String[]{"srparasites"}, SentientWeaponEvolutionHandler.class);
        registerIfModsPresent(new String[]{"srparasites", "biomesoplenty"}, SRParasitesHandler.class);

        MinecraftForge.EVENT_BUS.register(BlockBreakSlowHandler.class);
        registerIfModsPresent(new String[]{"nuclearcraft", "biomesoplenty"}, DoorDupeHandler.class);
        MinecraftForge.EVENT_BUS.register(AbyssalGateHandler.class);
        if(ForgeConfigHandler.irradiated.enabled) registerIfModsPresent(new String[]{"nuclearcraft"}, IrradiatedParasitesHandler.class);
        registerIfModsPresent(new String[]{"cookingforblockheads"}, TileCounterHandler.class);
        MinecraftForge.EVENT_BUS.register(new TeleportEvents());
        ForgeConfigHandler.refreshDrinkableBlockCache();
    }

    private static void registerIfModsPresent(String[] dependencies, Class<?> classToRegister){
        for (String dependency : dependencies)
            if(!Loader.isModLoaded(dependency)) return;
        MinecraftForge.EVENT_BUS.register(classToRegister);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

        eaglemixins.handlers.SpawnInjector.inject();

        net.minecraftforge.common.config.ConfigManager.sync(
                EagleMixins.MODID, net.minecraftforge.common.config.Config.Type.INSTANCE
        );

        BiomeTagHandler.init();
        RadiationResistanceRegistry.reloadFromConfig();
        if(Loader.isModLoaded("enhancedvisuals")) EnhancedVisualsHandler.init();
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

