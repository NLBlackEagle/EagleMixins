package eaglemixins.config;

import eaglemixins.config.folders.BerianConfig;
import eaglemixins.config.folders.ConductivityConfig;
import eaglemixins.config.folders.IrradiatedConfig;
import eaglemixins.config.folders.TippedArrowConfig;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import eaglemixins.EagleMixins;

@Config(modid = EagleMixins.MODID)
public class ForgeConfigHandler {

	@Config.Comment("Server-Side Options")
	@Config.Name("Server Options")
	public static final ServerConfig server = new ServerConfig();

	@Config.Comment("Client-Side Options")
	@Config.Name("Client Options")
	public static final ClientConfig client = new ClientConfig();

	@Config.Comment("Irradiated Options")
	@Config.Name("Irradiated Options")
	public static final IrradiatedConfig irradiated = new IrradiatedConfig();

	@Config.Comment("Conductivity Options")
	@Config.Name("Conductivity Options")
	public static final ConductivityConfig conductivity = new ConductivityConfig();

	@Config.Comment("Tipped Arrow Options")
	@Config.Name("Tipped Arrow Options")
	public static final TippedArrowConfig tippedarrows = new TippedArrowConfig();

	@Config.Comment("Berian Options")
	@Config.Name("Berian Options")
	public static final BerianConfig berian = new BerianConfig();

	public static class ServerConfig {
		@Config.Comment("List of mobs that players will not get dismounted from in Abyssal Rift")
		@Config.Name("Allowed Mounts in Abyssal Rift")
		public String[] allowedAbyssalMounts = {
				"minecraft:horse",
				"minecraft:donkey",
				"minecraft:pig",
				"minecraft:llama"
		};

		@Config.Comment("Parasites spawned in Abyssal Rift will have dmg that is this much higher than other overworld parasites")
		@Config.Name("Abyssal Rift Parasite Stat Multi: Dmg")
		public float abyssalDmgModifier = 1;

		@Config.Comment("Parasites spawned in Abyssal Rift will have health that is this much higher than other overworld parasites")
		@Config.Name("Abyssal Rift Parasite Stat Multi: HP")
		public float abyssalHPModifier = 1;

		@Config.Comment("Parasites spawned in Abyssal Rift will have armor that is this much higher than other overworld parasites")
		@Config.Name("Abyssal Rift Parasite Stat Multi: Armor")
		public float abyssalArmorModifier = 1;

		@Config.Comment("Cannot rename any mob with or to these names")
		@Config.Name("Blacklisted Name Change Any Mob")
		public String[] blackListEntitiesNameChangeAny = {
				"Dismounting",
				"Dismounter",
				"Dispel",
				"Sarevok",
				"Jester"
		};

		@Config.Comment("Cannot rename Player Bosses with or to these names")
		@Config.Name("Blacklisted Name Change Player Bosses")
		public String[] blackListEntitiesNameChangePlayerbosses = {
				"Blighted Shivaxi"
		};

		@Config.Comment("Cannot rename parasites with or to these names")
		@Config.Name("Blacklisted Name Change Parasite")
		public String[] blackListEntitiesNameChangeParasite = {
				"Sentient Horror",
				"Degrading Overseer",
				"Malformed Observer",
				"Shivaxi",
				"Corrupted Carrier",
				"Necrotic Blight"
		};

		@Config.Comment("Health multiplier for Blighted Shivaxi")
		@Config.Name("Blighted Shivaxi Health Multiplier")
		public float blightedShivaxiHealthModifier = 0.5F;

		@Config.Comment("Chance of an underground un-looted chest becoming a mimic")
		@Config.Name("Underground Mimic Chance")
		@Config.RangeDouble(min = 0D, max = 1D)
		public double undergroundMimicChance = 0.05D;

		@Config.Comment("List of dimension IDs in which underground chests will have a chance to become mimics")
		@Config.Name("Underground Mimic Dimensions")
		@Config.RequiresMcRestart
		public Integer[] undergroundMimicDimensions = {0};

		@Config.Comment("Prevents Observers from ticking a redstone pulse on world gen")
		@Config.Name("Patch Observer Ticking")
		public boolean patchObserversTickingOnWorldGen = false;

		@Config.Comment("Removes old item attributes (atk dmg and atk speed) from 1.0.4")
		@Config.Name("Remove old Attribute Modifiers")
		public boolean removeOldAttributes = true;
	}

	public static class ClientConfig {
		@Config.Comment("How many seconds to display each loadingscreen picture")
		@Config.Name("LoadingScreens: Frequency")
		public int frequency = 10;

		@Config.Comment("Whether the loadingscreen pictures are displayed in set order or randomised order")
		@Config.Name("LoadingScreens: Display in Random Order")
		public boolean randomOrder = true;

		@Config.Comment("Whether there should be only one loadingscreen picture displayed per loading, no cycling during load. Will always display first picture in list if random order config is off")
		@Config.Name("LoadingScreens: Don't Cycle")
		public boolean disableCycling = false;
	}

	@Mod.EventBusSubscriber(modid = EagleMixins.MODID)
	private static class EventHandler{

		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if(event.getModID().equals(EagleMixins.MODID)) {
				ConfigManager.sync(EagleMixins.MODID, Config.Type.INSTANCE);
				tippedarrows.reset();
				berian.reset();
				conductivity.reset();
				irradiated.reset();
			}
		}
	}

}