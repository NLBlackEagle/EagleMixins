package eaglemixins;

import eaglemixins.client.particles.ParticlesClientRunner;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.debug.BO3_ChunkGen_Debug;
import eaglemixins.handlers.*;
import eaglemixins.init.ModStats;
import eaglemixins.init.RadiationResistanceRegistry;
import eaglemixins.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = EagleMixins.MODID, version = EagleMixins.VERSION, name = EagleMixins.NAME, dependencies = "required-after:fermiumbooter@[1.3.2,)")
public class EagleMixins {

    public static final String MODID = "eaglemixins";
    public static final String VERSION = "1.2.0";
    public static final String NAME = "EagleMixins";
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public static final boolean debugEnabled = false;

    @Mod.Instance(value = MODID)
    public static EagleMixins INSTANCE;

	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        if(event.getSide() == Side.CLIENT) registerIfModsPresent(new String[]{"nuclearcraft"}, ParticlesClientRunner.class);

        MinecraftForge.EVENT_BUS.register(ProjectileImmunityHandler.class);
        registerIfModsPresent(new String[]{"firstaid"}, FirstAidRadiationHandler.class);
        registerIfModsPresent(new String[]{"nuclearcraft"}, ContainerNBTRadHandler.class);
        registerIfModsPresent(new String[]{"nuclearcraft"}, ItemRadCapAttacher.class);
        registerIfModsPresent(new String[]{"nuclearcraft"}, AttributeHandler.class);
        registerIfModsPresent(new String[]{"srparasites", "nuclearcraft"}, EntityBurningTickHandler.class);
        //MinecraftForge.EVENT_BUS.register(BlockNoclipHandler.class); //let ppl suffocate
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
        registerIfModsPresent(new String[]{"nuclearcraft"},RadiationResistanceApplier.class);
        MinecraftForge.EVENT_BUS.register(RandomTippedArrowHandler.class);
        if(ForgeConfigHandler.mixintoggles.punishAbyssalTeleportation) MinecraftForge.EVENT_BUS.register(RandomTpCancelHandler.class);
        MinecraftForge.EVENT_BUS.register(RecallFlightCancelHandler.class);
        if(ForgeConfigHandler.mixintoggles.customSRPGearEvolution) registerIfModsPresent(new String[]{"srparasites", "charm", "srpmixins"}, SentientWeaponEvolutionHandler.class);
        registerIfModsPresent(new String[]{"srparasites", "biomesoplenty"}, SRParasitesHandler.class);

        MinecraftForge.EVENT_BUS.register(PotionRadiationFatigueHandler.class);
        registerIfModsPresent(new String[]{"nuclearcraft", "biomesoplenty"}, DoorDupeHandler.class);
        MinecraftForge.EVENT_BUS.register(AbyssalGateHandler.class);
        if(ForgeConfigHandler.irradiated.enabled) registerIfModsPresent(new String[]{"nuclearcraft"}, IrradiatedParasitesHandler.class);
        registerIfModsPresent(new String[]{"cookingforblockheads"}, TileCounterHandler.class);
        MinecraftForge.EVENT_BUS.register(new TeleportEvents());
        ForgeConfigHandler.refreshDrinkableBlockCache();

        if(debugEnabled) {
            registerIfModsPresent(new String[]{"openterraingenerator"}, BO3_ChunkGen_Debug.class);
        }
    }

    private static void registerIfModsPresent(String[] dependencies, Class<?> classToRegister){
        for (String dependency : dependencies)
            if(!Loader.isModLoaded(dependency)) return;
        MinecraftForge.EVENT_BUS.register(classToRegister);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        BiomeTagHandler.init();
        RadiationResistanceRegistry.reloadFromConfig();
        if(Loader.isModLoaded("enhancedvisuals")) EnhancedVisualsHandler.init();
        EntitySpawnListener.init();
        ModStats.init();

        if (event.getSide().isClient()) {
            ForgeConfigHandler.loadParticleRulesFromConfig();
            registerIfModsPresent(new String[]{"nuclearcraft"}, ContainerNBTRadHandler.Tooltip.class);
        }

        PacketHandler.init();
    }
}

