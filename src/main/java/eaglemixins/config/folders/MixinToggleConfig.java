package eaglemixins.config.folders;

import eaglemixins.EagleMixins;
import fermiumbooter.annotations.MixinConfig;
import net.minecraftforge.common.config.Config;

@MixinConfig(name = EagleMixins.MODID)
@SuppressWarnings("unused")
public class MixinToggleConfig {
    @Config.Comment("Will stop disarming mobs with living/sentient or dragonbone gear using the BS Disarm or the SME Disarmament enchantment.")
    @Config.Name("Stop disarming some gear (BS/SME/RLCombat)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.various.stopdisarming.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "mujmajnkraftsbettersurvival", desired = true, reason = "Requires mod to properly function")
    @MixinConfig.CompatHandling(modid = "somanyenchantments", desired = true, reason = "Requires mod to properly function")
    @MixinConfig.CompatHandling(modid = "bettercombatmod", desired = true, reason = "Requires RLCombat to properly function")
    public boolean stopDisarming = true;

    @Config.Comment("Renames Varied Commodities Ruby to gem_ruby_vc to avoid mix-ups.")
    @Config.Name("Rename Ruby (VC)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.vc.rubyrename.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "variedcommodities", desired = true, reason = "Requires mod to properly function")
    public boolean renameRuby = true;

    @Config.Comment("Treats player bosses differently if they generated in the Abyssal Rift biome.")
    @Config.Name("Abyssal Player Bosses (Giant Player Boss)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.playerbosses.abyssalriftspawn.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "playerbosses", desired = true, reason = "Requires mod to properly function")
    public boolean abyssalPlayerBosses = true;

    @Config.Comment("Allows to change between loading screens with a modifiable waiting time between screens.")
    @Config.Name("Cycle Screens (Loading Screens)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.loadingscreens.cyclescreens.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "loadingscreens", desired = true, reason = "Requires mod to properly function")
    public boolean cycleLoadingScreens = true;

    @Config.Comment("Will show the progress for unpacking DregoraRL during first time startup.")
    @Config.Name("First Time Setup Progress (DregoraRL)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.dregorarl.firsttimesetup.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "dregorarl", desired = true, reason = "Requires mod to properly function")
    public boolean firstTimeStartupProgress = true;

    @Config.Comment("Allows to separately modify chance for mimics for unlooted underground chests specifically.")
    @Config.Name("Underground Mimics (RLArtifacts)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.rlartifacts.undergroundmimics.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "artifacts", desired = true, reason = "Requires mod to properly function")
    public boolean undergroundMimics = true;

    @Config.Comment("Allows to modify what kind of blocks simplediff will see as being able to drink from.")
    @Config.Name("Extend Drinkable Blocks (SimpleDifficulty)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.simpledifficulty.drinkableblocks.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "simpledifficulty", desired = true, reason = "Requires mod to properly function")
    public boolean extendDrinkableBlocks = true;

    @Config.Comment("If players use teleportation methods in Abyssal Rift, they will get potion effects applied to prevent them to cheese brutal towers.")
    @Config.Name("Prevent Abyssal Teleportation (Vanilla/Trinkets)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(
            earlyMixin = "mixins.eaglemixins.vanilla.randomtp.json",
            lateMixin = "mixins.eaglemixins.xat.randomtp.json",
            defaultValue = true
    )
    @MixinConfig.CompatHandling(modid = "xat", desired = true, reason = "Requires mod to properly function")
    @MixinConfig.CompatHandling(modid = "potioncore", desired = true, reason = "Requires mod to properly function")
    public boolean punishAbyssalTeleportation = true;

    @Config.Comment("Compared to SRPs system, this will only count parasite kills, distribute the killed mobs HP over the equipped SRP living gear and also evolve living bows and living armor.")
    @Config.Name("Custom Living Gear Evolution (SRP)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.srparasites.disabledefaultgearevo.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "srparasites", desired = true, reason = "Requires mod to properly function")
    @MixinConfig.CompatHandling(modid = "charm", disableMixin = false, desired = true, reason = "Requires mod to properly function")
    @MixinConfig.CompatHandling(modid = "srpmixins", disableMixin = false, desired = true, reason = "Optional compat", warnIngame = false)
    public boolean customSRPGearEvolution = true;

    @Config.Comment("Allow NuclearCraft blocks with a Reskillable level requirement to still be right click interactable.")
    @Config.Name("Override Block Interaction Lock (Reskillable)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.reskillable.rightclickblockoverride.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "reskillable", desired = true, reason = "Requires mod to properly function")
    @MixinConfig.CompatHandling(modid = "nuclearcraft", disableMixin = false, desired = true, reason = "Mixin not needed without NuclearCraft", warnIngame = false)
    public boolean overrideReskillableLock = true;

    @Config.Comment("Change NuclearCraft chunk radiation to be variable over subchunks (16 blocks high slices of a chunk).")
    @Config.Name("SubChunk Radiation (NuclearCraft)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.nuclearcraft.subchunkradiation.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "nuclearcraft", desired = true, reason = "Requires mod to properly function")
    public boolean subchunkRadiation = true;

    @Config.Comment("Will multiply all rads by 1000 to make them better readable. Also will stop displaying negligible radiation in item tooltips.")
    @Config.Name("Change Rads Scale (NuclearCraft)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.nuclearcraft.radiationscalechange.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "nuclearcraft", desired = true, reason = "Requires mod to properly function")
    public boolean radiationScaleChange = true;

    @Config.Comment("Allows to modify the radiation some fluid blocks give off, making them work similar to Corium fluid.")
    @Config.Name("Radiating Fluids (NuclearCraft)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.nuclearcraft.radiatingfluids.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "nuclearcraft", desired = true, reason = "Requires mod to properly function")
    public boolean radiatingFluids = true;

    @Config.Comment("Fixes Hazmat Suit taking durability dmg from armor ignoring dmg like poison etc.")
    @Config.Name("Fix Hazmat Dmg (NuclearCraft)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.nuclearcraft.fixhazmatdmg.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "nuclearcraft", desired = true, reason = "Requires mod to properly function")
    public boolean fixHazmatDmg = true;

    @Config.Comment("Makes EagleMixins Radiation Resistance attribute reduce the radiation an entity is receiving.")
    @Config.Name("Rad Resistance Attribute (NuclearCraft)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.nuclearcraft.radresistanceattribute.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "nuclearcraft", desired = true, reason = "Requires mod to properly function")
    public boolean radResistanceAttribute = true;

    @Config.Comment("Fixes potion effects applied by NuclearCraft via config onto irradiated entities having a too short duration, not showing particles and using the potion level instead of amplifier.")
    @Config.Name("Fix Radiation Effects (NuclearCraft)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.nuclearcraft.radiationeffects.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "nuclearcraft", desired = true, reason = "Requires mod to properly function")
    public boolean fixRadiationEffects = true;

    @Config.Comment("Makes armor keep its current durability when applying radiation shielding.")
    @Config.Name("Fix Shielding Recipe (NuclearCraft)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.nuclearcraft.shieldingrecipe.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "nuclearcraft", desired = true, reason = "Requires mod to properly function")
    public boolean fixShieldingRecipe = true;

    @Config.Comment("Allows to define radiation values for containers with loot tables that havent generated loot yet. Also fixes NuclearCraft automatically generating the loot in such chests.")
    @Config.Name("Loot Table Radiation (NuclearCraft)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.nuclearcraft.loottableradiation.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "nuclearcraft", desired = true, reason = "Requires mod to properly function")
    public boolean lootTableRadiation = true;

    @Config.Comment("Will make a lot of otherwise safe inventories also irradiate the player and the area if they have irradiated items, such as Shulker Boxes and Charm Crates, worn Backpacks and opened Ender Chests. Also takes care of the one item a player can hold with their mouse when having a GUI open, and the 2x2 crafting matrix in the players inventory.")
    @Config.Name("Inventory Radiation (NuclearCraft/Charm/WearableBackpacks)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.nuclearcraft.inventoryradiation.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "nuclearcraft", desired = true, reason = "Requires mod to properly function")
    @MixinConfig.CompatHandling(modid = "charm", desired = true, reason = "Requires mod to properly function")
    @MixinConfig.CompatHandling(modid = "wearablebackpacks", desired = true, reason = "Requires mod to properly function")
    public boolean inventoryRadiation = true;

    @Config.Comment("Make players regain their air slowly when resurfacing from submerged - instead of regaining it instantly.")
    @Config.Name("Slow Air Refill (Vanilla)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(earlyMixin = "mixins.eaglemixins.vanilla.slowairrefill.json", defaultValue = true)
    public boolean slowAirRefill = true;

    @Config.Comment("Allow NBT (loot tables etc) using vanillas RandomValueRange to have min > max. Swap those in that case so the given min value will be interpreted as max and vice versa.")
    @Config.Name("Fix Eagle Math (Vanilla)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(earlyMixin = "mixins.eaglemixins.vanilla.randomvaluerange_eaglemath.json", defaultValue = true)
    public boolean fixEagleMath = true;

    @Config.Comment("Make Sussyberians and Mentalberians keep their berian state over zombification and curing.")
    @Config.Name("Zombie Berians (Vanilla)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(earlyMixin = "mixins.eaglemixins.vanilla.zombieberians.json", defaultValue = true)
    public boolean zombieBerians = true;

    @Config.Comment("Extend climbable property of open trapdoors to needing any ladder below the trapdoor instead of just vanilla ladders.")
    @Config.Name("Climbable Trapdoors (Vanilla)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(earlyMixin = "mixins.eaglemixins.vanilla.climbabletrapdoors.json", defaultValue = true)
    public boolean climbableTrapdoors = true;

    @Config.Comment("Renders GUI hearts differently if the player is irradiated.")
    @Config.Name("Radiation Hearts (Vanilla)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(earlyMixin = "mixins.eaglemixins.vanilla.customradiationhearts.json", defaultValue = true)
    public boolean customRadiationHearts = true;

    @Config.Comment("Fixes forge ignoring default attributes (atk dmg/speed/armor/toughness) of gear if there is additional attributes set via the set_attributes loot function. Also allows to fix old gear from dregora 1.0.4 (deprecated, should be removed soon).")
    @Config.Name("Item Attributes (Vanilla)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(earlyMixin = "mixins.eaglemixins.vanilla.itemattributes.json", defaultValue = true)
    public boolean itemAttributes = true;

    @Config.Comment("Prevents Observers from ticking a redstone pulse on world gen")
    @Config.Name("Patch Observer Ticking (Vanilla)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(earlyMixin = "mixins.eaglemixins.vanilla.observertickonchunkgen.json", defaultValue = false)
    public boolean patchObserversTickingOnWorldGen = false;

    @Config.Comment("Ice upgrades for the fridge will also treat SimpleDifficulty ice chunks as ice.")
    @Config.Name("More Ice in Fridge (CookingForBlockheads)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.cookingforblockheads.moreice.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "cookingforblockheads", desired = true, reason = "Requires mod to properly function")
    public boolean moreIceInFridge = true;

    @Config.Comment("Makes all CookingForBlockheads counters allow to have generatable loot using the vanilla loot table system.")
    @Config.Name("Counters allow Loot Tables (CookingForBlockheads)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.cookingforblockheads.loottables.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "cookingforblockheads", desired = true, reason = "Requires mod to properly function")
    public boolean counterLootTables = true;

    @Config.Comment("Allows CookingForBlockheads counters to have custom inventory names using the vanilla system (rename the item and place it).")
    @Config.Name("Custom Inventory Names (CookingForBlockheads)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.cookingforblockheads.custominventorynames.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "cookingforblockheads", desired = true, reason = "Requires mod to properly function")
    public boolean customInventoryNames = true;

    @Config.Comment("Caches sourceBlock hashes to greatly reduce performance for some OTG BO3 type structures. This skips the unnecessarily expensive MaterialSet.contains() call.")
    @Config.Name("BO3 Material Performance (OTG)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.otg.bo3materialperformance.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "openterraingenerator", desired = true, reason = "Requires mod to properly function")
    public boolean bo3getMaterialPerformanceFix = true;

    @Config.Comment("Caches BlockFunctions for some (rotatingRandomly=false) BO3 type OTG structures instead of gathering them again every time that structure generates.")
    @Config.Name("BO3 BlockFunction Cache (OTG)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.otg.bo3blockfunctioncache.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "openterraingenerator", desired = true, reason = "Requires mod to properly function")
    public boolean bo3blockFunctionCache = true;

    @Config.Comment("Modifies the world creation process to limit users to creating only the preset world type")
    @Config.Name("Create World Simplification (OTG)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.otg.createworldsimplify.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "openterraingenerator", desired = true, reason = "Requires mod to properly function")
    public boolean otgCreateWorldFix = true;

    @Config.Comment("Prevents Fish's Undead Rising Undertaker, Avaton and Skeleton King summons from dropping loot or XP.")
    @Config.Name("Cancel Summon Loot & XP (FUR)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.fishsundeadrising.nolootforsummons.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "mod_lavacow", desired = true, reason = "Requires mod to properly function")
    public boolean noLootForFURSummons = true;

    @Config.Comment("Changes how Fish's Undead Rising Skeleton Kings are summoned. Allows biomes with at least one of the following BiomeDictionary types: MESA, SAVANNA, SANDY or DEAD.")
    @Config.Name("Modify Skeleton King (FUR)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.fishsundeadrising.modifyskeletonking.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "mod_lavacow", desired = true, reason = "Requires mod to properly function")
    public boolean modifiedSkeletonKing = true;

    @Config.Comment("Cancel Fish's Undead Rising Reaper Scythe custom sweep handling.")
    @Config.Name("Cancel Reaper Scythe Custom Sweep (FUR)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.fishsundeadrising.cancelcustomsweep.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "mod_lavacow", desired = true, reason = "Requires mod to properly function")
    public boolean cancelCustomSweep = true;

    @Config.Comment("Fish's Undead Rising Infested effect spawns FUR parasites when undead mobs die. This limits how many parasites can spawn from one entity.")
    @Config.Name("Limit Infested Effect (FUR)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.fishsundeadrising.limitparasiteinfestation.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "mod_lavacow", desired = true, reason = "Requires mod to properly function")
    public boolean limitInfestedEffect = true;

    @Config.Comment("By default, Legendary Tooltips only allows 16 different tooltip frames. This increases that max to 64.")
    @Config.Name("More Frames (LegendaryTooltips)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(lateMixin = "mixins.eaglemixins.legendarytooltips.moreframes.json", defaultValue = true)
    @MixinConfig.CompatHandling(modid = "legendarytooltips", desired = true, reason = "Requires mod to properly function")
    public boolean moreLegendaryTooltipFrames = true;

    @Config.Comment("Allows to modify mob equipment (mainly zombies and skeletons) set in the \"Mob Equipment\" config.")
    @Config.Name("Mob Equipment Modification (Vanilla)")
    @Config.RequiresMcRestart
    @MixinConfig.MixinToggle(earlyMixin = "mixins.eaglemixins.vanilla.mobequipment.json", defaultValue = true)
    public boolean mobEquipmentModify = true;
}
