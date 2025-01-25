package eaglemixins.config;

import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import eaglemixins.EagleMixins;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.Level;

import java.util.*;

@Config(modid = EagleMixins.MODID)
public class ForgeConfigHandler {

	@Config.Comment("Server-Side Options")
	@Config.Name("Server Options")
	public static final ServerConfig server = new ServerConfig();

	@Config.Comment("Client-Side Options")
	@Config.Name("Client Options")
	public static final ClientConfig client = new ClientConfig();

	public static class ServerConfig {
		@Config.Comment("List of mobs that players will not get dismounted from in Abyssal Rift")
		@Config.Name("Allowed Mounts in Abyssal Rift")
		public String[] allowedAbyssalMounts = {
				"minecraft:horse",
				"minecraft:donkey",
				"minecraft:pig",
				"minecraft:llama"
		};

		@Config.Comment("List of mobs that can spawn FUR Parasites on death")
		@Config.Name("Spawns FUR Parasite")
		public String[] spawnsFURParasitesOnDeath = {
				"minecraft:zombie",
				"minecraft:husk",
				"mod_lavacow:zombiefrozen",
				"mod_lavacow:zombiemushroom",
				"mod_lavacow:unburied",
				"mod_lavacow:mummy"
		};

		@Config.Comment("The contents of this list will be matched against the item ids names (not the mod name). Bad for performance if you overfill this list! Pattern [itemidpart conductivity]")
		@Config.Name("Material Conductivity")
		String[] materialConductivities = {
				"iron 1",
				"steel 1",
				"chainmail 1",
				"bucket 1",
				"bronze 2",
				"gold 3",
				"cincinnasite 3",
				"summoningstaff 3",
				"copper 4",
				"silver 5",
				"lightningdragonbone 10",
				"lightning_dragonbone 10"
		};

		@Config.Comment("List of item ids and their conductivity, separated by a space")
		@Config.Name("Item Conductivity")
		String[] itemConductivities = {
				"minecraft:compass 1",
				"minecraft:shears 1",
				"minecraft:shield 1",
				"mujmajnkraftsbettersurvival:itemcrossbow 1",
				"mujmajnkraftsbettersurvival:itemsmallshield 1",
				"mujmajnkraftsbettersurvival:itembigshield 1",
				"forgottenitems:bound_pickaxe 1",
				"forgottenitems:bound_axe 1",
				"forgottenitems:bound_shovel 1",
				"grapplemod:grapplinghook 1",
				"iceandfire:dread_sword 1",
				"iceandfire:dread_knight_sword 1",
				"iceandfire:troll_weapon.axe 1",
				"iceandfire:troll_weapon.hammer 1",
				"rustic:candle 1",
				"rustic:candle_lever 1",
				"simpledifficulty:iron_canteen 1",
				"spartanweaponry:caestus_studded 1",
				"switchbow:switchcrossbow 1",
				"variedcommodities:hammer 1",
				"variedcommodities:lead_pipe 1",
				"variedcommodities:crowbar 1",
				"variedcommodities:pipe_wrench 1",
				"variedcommodities:wrench 1",
				"variedcommodities:candle 1",
				"variedcommodities:lamp 1",
				"variedcommodities:chain_skirt 1",

				"forgottenitems:hasty_pickaxe 3",
				"variedcommodities:holyhandgrenade 3",
				"variedcommodities:pendant 3",
				"mod_lavacow:holy_grenade 3",
				"mod_lavacow:kings_crown 3",
				"minecraft:clock 3",
				"inspirations:redstone_charger 3",
				"trumpetskeleton:trumpet 3",
				"mod_lavacow:skeletonking_crown 3",
				"mod_lavacow:kings_crown:1 3",
				"locks:master_key 3",
				"locks:key 3",
				"armorunder:liner_snips 3",

				"charm:charged_emerald 10",
				"aquaculture:fish:21 10",
				"advanced-fishing:fish:18 10",
				"iceandfire:dragonbone_sword_lightning 10",

				"mujmajnkraftsbettersurvival:itemlumiumhammer 100"
		};

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

		@Config.Comment("Chance for an entity to have its arrow replaced with a tipped arrow")
		@Config.Name("Tipped Arrow Replacement Chance")
		@Config.RangeDouble(min = 0.0F, max = 1.0F)
		public float tippedArrowReplacementChance = 0.05F;

		@Config.Comment("List of entities to allow randomly adding tipped arrows")
		@Config.Name("Tipped Arrow Replacement Allowed Entities")
		public String[] tippedArrowEntities = {
				"minecraft:skeleton",
				"minecraft:wither_skeleton"
		};

		@Config.Comment("List of long potion types to be used for tipped arrows randomly added to entities")
		@Config.Name("Tipped Arrow Replacement Allowed PotionTypes Long")
		public String[] tippedArrowPotionsLong = {
				"potioncore:long_klutz",
				"potioncore:long_weight",
				"potioncore:long_broken_armor",
				"potioncore:long_spin",
				"quark:long_mining_fatigue",
				"potioncore:long_magic_inhibition",
				"potioncore:long_drown",
				"potioncore:long_vulnerable",
				"potioncore:long_rust",
				"potioncore:long_perplexity",
				"minecraft:long_slowness",
				"mujmajnkraftsbettersurvival:long_antiwarp",
				"mujmajnkraftsbettersurvival:long_decay",
				"mujmajnkraftsbettersurvival:long_blindness",
				"potioncore:long_blindness",
				"potioncore:long_nausea",
				"potioncore:long_levitation",
				"potioncore:long_hunger",
				"potioncore:long_wither",
				"elenaidodge:long_sluggish",
				"minecraft:long_poison",
				"minecraft:long_weakness",
				"elenaidodge:long_feeble",
				"potioncore:long_broken_magic_shield"
		};

		@Config.Comment("List of non-long potion types to be used for tipped arrows randomly added to entities")
		@Config.Name("Tipped Arrow Replacement Allowed PotionTypes Short")
		public String[] tippedArrowPotions = {
				"potioncore:strong_broken_armor",
				"potioncore:broken_armor",
				"potioncore:strong_klutz",
				"potioncore:klutz",
				"potioncore:dispel",
				"potioncore:strong_launch",
				"potioncore:launch",
				"potioncore:spin",
				"potioncore:strong_spin",
				"potioncore:curse",
				"potioncore:strong_curse",
				"quark:mining_fatigue",
				"quark:strong_mining_fatigue",
				"potioncore:disorganization",
				"srparasites:foster",
				"srparasites:coth",
				"srparasites:fear",
				"srparasites:res",
				"srparasites:corro",
				"srparasites:vira",
				"srparasites:rage",
				"srparasites:debar",
				"potioncore:magic_inhibition",
				"potioncore:weight",
				"potioncore:lightning",
				"potioncore:strong_explode",
				"potioncore:teleport",
				"potioncore:strong_teleport",
				"potioncore:teleport_surface",
				"potioncore:drown",
				"potioncore:teleport_spawn",
				"potioncore:strong_vulnerable",
				"potioncore:vulnerable",
				"potioncore:strong_rust",
				"potioncore:rust",
				"potioncore:perplexity",
				"minecraft:slowness",
				"mujmajnkraftsbettersurvival:milk",
				"mujmajnkraftsbettersurvival:antiwarp",
				"mujmajnkraftsbettersurvival:strong_decay",
				"mujmajnkraftsbettersurvival:decay",
				"potioncore:nausea",
				"potioncore:levitation",
				"potioncore:strong_levitation",
				"potioncore:unluck",
				"potioncore:strong_hunger",
				"potioncore:hunger",
				"potioncore:strong_wither",
				"potioncore:wither",
				"elenaidodge:sluggish",
				"elenaidodge:strong_feeble",
				"minecraft:harming",
				"minecraft:strong_harming",
				"minecraft:poison",
				"minecraft:strong_poison",
				"mujmajnkraftsbettersurvival:blindness",
				"elenaidodge:feeble",
				"potioncore:strong_magic_inhibition",
				"potioncore:strong_weight",
				"potioncore:fire",
				"potioncore:invert",
				"potioncore:broken_magic_shield",
				"potioncore:strong_broken_magic_shield",
				"potioncore:strong_blindness",
				"potioncore:blindness",
				"potioncore:explode",
				"xat:extended_goblin",
				"xat:goblin"
		};

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
		public Integer[] undergroundMimicDimensions = {0};

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

		@Config.Comment("Removes old item attributes (atk dmg and atk speed) from 1.0.4")
		@Config.Name("Remove old Attribute Modifiers")
		public boolean removeOldAttributes = true;
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
				arrowAllowedEntities = null;
				conductivityMap = null;
			}
		}
	}

	private static Map<Item, Integer> conductivityMap;
	public static int getItemConductivity(Item item) {
		if (conductivityMap == null) {
			conductivityMap = new HashMap<>();
			for (String entry : server.itemConductivities) {
				String[] split = entry.split(" ");
				if (split.length != 2) continue;
				Item itemInConfig = Item.getByNameOrId(split[0].trim());
				if (itemInConfig != null) {
					try {
						conductivityMap.put(itemInConfig, Integer.parseInt(split[1].trim()));
					} catch (Exception exception) {
						EagleMixins.LOGGER.error("Failed parsing item conductivity ({})", entry);
					}
				}
			}
		}

		//Map contains the item already
		if (conductivityMap.containsKey(item))
			return conductivityMap.get(item);

		//Check if one of the materials fits and save in map
		ResourceLocation itemId = item.getRegistryName();
		if (itemId != null) {
			for (int i = 0; i < server.materialConductivities.length; i++) {
				String[] split = server.materialConductivities[i].split(" ");
				if (split.length != 2) continue;
				if (itemId.getPath().contains(split[0].trim())) {
					//TODO catch parsing error
					try {
						int conductivity = Integer.parseInt(split[1].trim());
						conductivityMap.put(item, conductivity);
						return conductivity;
					} catch (Exception exception) {
						EagleMixins.LOGGER.error("Failed parsing material conductivity line #{} ({})", i, server.materialConductivities[i]);
					}
				}
			}
		}
		//Not found, save 0 for this item
		conductivityMap.put(item, 0);
		return 0;
	}

	private static HashSet<ResourceLocation> arrowAllowedEntities = null;
	public static HashSet<ResourceLocation> getArrowAllowedEntities() {
		if(arrowAllowedEntities == null) {
			HashSet<ResourceLocation> set = new HashSet<>();
			for(String entity : server.tippedArrowEntities) {
				set.add(new ResourceLocation(entity));
			}
			arrowAllowedEntities = set;
		}
		return arrowAllowedEntities;
	}

	private static List<ItemStack> tippedArrowArrayLong = null;
	private static List<ItemStack> tippedArrowArray = null;
	public static ItemStack getRandomArrowStack(Random rand, boolean isLong){
		//Lazy loading
		if(isLong && tippedArrowArrayLong == null){
			tippedArrowArrayLong = new ArrayList<>();
			for(String potionString : server.tippedArrowPotionsLong) {
				PotionType type = PotionType.getPotionTypeForName(potionString);
				if(type == null){
					EagleMixins.LOGGER.warn("RLMixins Dregora Arrow PotionTypes invalid PotionType: " + potionString + ", ignoring.");
					continue;
				}
				tippedArrowArrayLong.add(PotionUtils.addPotionToItemStack(new ItemStack(Items.TIPPED_ARROW),type));
			}
		}
		if(!isLong && tippedArrowArray == null){
			tippedArrowArray = new ArrayList<>();
			for(String potionString : server.tippedArrowPotions) {
				PotionType type = PotionType.getPotionTypeForName(potionString);
				if(type == null){
					EagleMixins.LOGGER.warn("RLMixins Dregora Arrow PotionTypes invalid PotionType: " + potionString + ", ignoring.");
					continue;
				}
				tippedArrowArray.add(PotionUtils.addPotionToItemStack(new ItemStack(Items.TIPPED_ARROW),type));
			}
		}
		if(isLong) return tippedArrowArrayLong.get(rand.nextInt(tippedArrowArrayLong.size())).copy();
		else       return tippedArrowArray.get(rand.nextInt(tippedArrowArray.size())).copy();
	}

	private static List<Potion> sussyberianEffects = null;
	private static List<Potion> mentalberianEffects = null;
	private static Potion berianConstantEffect = null;
	
	public static List<Potion> getSussyberianEffects() {
		if(sussyberianEffects == null) {
			sussyberianEffects = new ArrayList<>();
			for(String name : server.sussyberianEffects) {
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
			for(String name : server.mentalberianEffects) {
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
			String name = server.berianConstantEffect.trim();
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