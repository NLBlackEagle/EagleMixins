package eaglemixins.config;

import eaglemixins.config.folders.*;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import eaglemixins.EagleMixins;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Config(modid = EagleMixins.MODID)
public class ForgeConfigHandler {

	@Config.Ignore
	public static final Set<String> cachedDrinkableBlocks = new HashSet<>();

	@Config.Comment("Server-Side Options")
	@Config.Name("Server Options")
	public static final ServerConfig server = new ServerConfig();

	@Config.Comment("Client-Side Options")
	@Config.Name("Client Options")
	public static final ClientConfig client = new ClientConfig();

	@Config.Comment("Abyssal Rift Options")
	@Config.Name("Abyssal Rift Options")
	public static final AbyssalConfig abyssal = new AbyssalConfig();

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

	@Config.Comment("SRParasites Options")
	@Config.Name("SRParasites Options")
	public static final SRParasiteConfig srparasites = new SRParasiteConfig();

	public static class ServerConfig {

		@Config.Comment("Chance of teleporting player to Underneath for exotic teleportation (through the concrete teleporters)")
		@Config.Name("Teleportation Underneath chance 0 to 100")
		public int teleportation_chance = 1;

		@Config.Comment({
				"Radiation resistance per entity.",
				"Format: <entity_id>=<value>",
				"Example: minecraft:sheep=1.0"
		})
		@Config.Name("RadiationResistanceList")
		public String[] radiationResistanceList = new String[] {
				"iceandfire:firedragon=1000.0",
				"iceandfire:icedragon=1000.0",
				"iceandfire:lightningdragon=1000.0"
		};

		@Config.Comment("Add Blocks you can drink from, like water")
		@Config.Name("Additional Water Blocks:")
		public String[] waterblockListdrinkables = {
				"cookingforblockheads:sink"
		};

		@Config.Comment("Give Dismounting entities the ability to dismount players when they target a player in Abyssal Rift or Parasite biomes")
		@Config.Name("Dismount on target:")
		public boolean dismounterTarget = true;

		@Config.Comment("Cannot rename any mob with or to these names")
		@Config.Name("Blacklisted Name Change Any Mob")
		public String[] blackListEntitiesNameChangeAny = {
				"Dismounting",
				"Dismounter",
				"Dispel",
				"Sarevok",
				"IHaveNoClue",
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

		@Config.Name("Fix Biomes O Plenty Door Duplication")
		@Config.Comment("Prevents Biomes O Plenty doors from dropping twice when broken")
		public static boolean fixBOPDoorDupe = true;

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

	public static void refreshDrinkableBlockCache() {
		cachedDrinkableBlocks.clear();

		if (server.waterblockListdrinkables != null) {
			for (String s : server.waterblockListdrinkables) {
				cachedDrinkableBlocks.add(s.toLowerCase(Locale.ROOT));
			}
		}
	}

	@Mod.EventBusSubscriber(modid = EagleMixins.MODID)
	public static class EventHandler {

		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if(event.getModID().equals(EagleMixins.MODID)) {
				ConfigManager.sync(EagleMixins.MODID, Config.Type.INSTANCE);
				tippedarrows.reset();
				berian.reset();
				conductivity.reset();
				irradiated.reset();
				srparasites.reset();
				ForgeConfigHandler.refreshDrinkableBlockCache();
			}
		}
	}
}