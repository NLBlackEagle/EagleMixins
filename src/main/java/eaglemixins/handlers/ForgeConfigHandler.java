package eaglemixins.handlers;

import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import eaglemixins.EagleMixins;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;

@Config(modid = EagleMixins.MODID)
public class ForgeConfigHandler {
	
	@Config.Comment("Server-Side Options")
	@Config.Name("Server Options")
	public static final ServerConfig server = new ServerConfig();

	@Config.Comment("Client-Side Options")
	@Config.Name("Client Options")
	public static final ClientConfig client = new ClientConfig();

	public static class ServerConfig {

		@Config.Comment("Chance of a librarian being converted to a Sussyberian on generation")
		@Config.Name("Sussyberian Chance")
		@Config.RangeDouble(min = 0D, max = 1D)
		public double sussyberianChance = 0.05D;

		@Config.Comment("Chance of a librarian being converted to a Mentalberian on generation")
		@Config.Name("Mentalberian Chance")
		@Config.RangeDouble(min = 0D, max = 1D)
		public double mentalberianChance = 0.05D;

		@Config.Comment("Chance of an underground un-looted chest becoming a mimic")
		@Config.Name("Underground Mimic Chance")
		@Config.RangeDouble(min = 0D, max = 1D)
		public double undergroundMimicChance = 0.05D;

		@Config.Comment("List of dimension IDs in which underground chests will have a chance to become mimics")
		@Config.Name("Underground Mimic Dimensions")
		@Config.RequiresMcRestart
		public Integer[] undergroundMimicDimensions = { 0 };
		
		@Config.Comment("List of potion effects that sussyberians will give")
		@Config.Name("Sussyberian Random Effect List")
		public String[] sussyberianEffects = {
				"potioncore:lightning",
				"potioncore:explode",
				"potioncore:explode",
				"potioncore:explode",
				"potioncore:explode",
				"potioncore:explode",
				"potioncore:explode",
				"potioncore:launch",
				"potioncore:launch",
				"potioncore:launch",
				"potioncore:launch",
				"potioncore:launch",
				"potioncore:launch"
		};
		
		@Config.Comment("List of potion effects that mentalberians will randomly give")
		@Config.Name("Mentalberian Random Effect List")
		public String[] mentalberianEffects = {
				"minecraft:nausea",
				"minecraft:mining_fatigue",
				"minecraft:blindness",
				"minecraft:unluck",
				"lycanitesmobs:paralysis",
				"lycanitesmobs:insomnia",
				"lycanitesmobs:fear",
				"lycanitesmobs:aphagia",
				"potioncore:klutz",
				"potioncore:spin",
				"potioncore:disorganization",
				"rustic:tipsy",
				"elenaidodge:sluggish",
				"srparasites:fear"
		};
		
		@Config.Comment("Effect that clicking on berians will always give")
		@Config.Name("Berian Constant Effect")
		public String berianConstantEffect = "mod_lavacow:soiled";
		
		@Config.Comment("Prevents Observers from ticking a redstone pulse on world gen")
		@Config.Name("Patch Observer Ticking")
		public boolean patchObserversTickingOnWorldGen = false;
	}

	public static class ClientConfig {

		@Config.Comment("Example client side config option")
		@Config.Name("Example Client Option")
		public boolean exampleClientOption = true;
	}

	@Mod.EventBusSubscriber(modid = EagleMixins.MODID)
	private static class EventHandler{

		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if(event.getModID().equals(EagleMixins.MODID)) {
				ConfigManager.sync(EagleMixins.MODID, Config.Type.INSTANCE);
				mentalberianEffects = null;
				sussyberianEffects = null;
				berianConstantEffect = null;
			}
		}
	}
	
	private static List<Potion> sussyberianEffects = null;
	private static List<Potion> mentalberianEffects = null;
	private static Potion berianConstantEffect = null;
	
	public static List<Potion> getSussyberianEffects() {
		if(sussyberianEffects == null) {
			sussyberianEffects = new ArrayList<>();
			for(String name : ForgeConfigHandler.server.sussyberianEffects) {
				if(name.trim().isEmpty()) continue;
				Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(name.trim()));
				if(potion == null) {
					EagleMixins.LOGGER.log(Level.WARN, "Unable to find potion effect: " + name);
					continue;
				}
				sussyberianEffects.add(potion);
			}
		}
		return sussyberianEffects;
	}
	
	public static List<Potion> getMentalberianEffects() {
		if(mentalberianEffects == null) {
			mentalberianEffects = new ArrayList<>();
			for(String name : ForgeConfigHandler.server.mentalberianEffects) {
				if(name.trim().isEmpty()) continue;
				Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(name.trim()));
				if(potion == null) {
					EagleMixins.LOGGER.log(Level.WARN, "Unable to find potion effect: " + name);
					continue;
				}
				mentalberianEffects.add(potion);
			}
		}
		return mentalberianEffects;
	}
	
	public static Potion getBerianConstantEffect() {
		if(berianConstantEffect == null) {
			String name = ForgeConfigHandler.server.berianConstantEffect.trim();
			Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(name));
			if(potion == null) {
				EagleMixins.LOGGER.log(Level.WARN, "Unable to find potion effect: " + name);
				berianConstantEffect = MobEffects.NAUSEA;
			}
			else berianConstantEffect = potion;
		}
		return berianConstantEffect;
	}
}