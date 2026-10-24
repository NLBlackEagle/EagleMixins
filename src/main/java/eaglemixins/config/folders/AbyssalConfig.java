package eaglemixins.config.folders;

import eaglemixins.handlers.RandomTpCancelHandler;
import net.minecraftforge.common.config.Config;

public class AbyssalConfig {
    @Config.Comment("All Nexus (Dispatcher/Beckon) parasites in Abyssal Rift will automatically be killed")
    @Config.Name("Kill Nexus in Abyssal Rift")
    public boolean killAbyssalNexus = true;

    @Config.Comment("Enchanted book drops in Abyssal Rift have only this chance to actually drop (reduced due to increased champion amounts)")
    @Config.Name("Abyssal Enchant Drop Chance")
    public float chanceEnchants = 0.3f;

    @Config.Comment("List of mobs that players will not get dismounted from in Abyssal Rift")
    @Config.Name("Allowed Mounts in Abyssal Rift")
    public String[] allowedAbyssalMounts = {
            "minecraft:horse",
            "minecraft:donkey",
            "minecraft:pig",
            "minecraft:llama",
            "lycanitesmobs:barghest",
            "lycanitesmobs:erepede",
            "lycanitesmobs:eyewig",
            "lycanitesmobs:feradon",
            "lycanitesmobs:ignibus",
            "lycanitesmobs:maug",
            "lycanitesmobs:pinky",
            "lycanitesmobs:salamander",
            "lycanitesmobs:shade",
            "lycanitesmobs:uvaraptor",
            "lycanitesmobs:ventoraptor",
            "lycanitesmobs:warg",
            "lycanitesmobs:zoataur",
            "mod_lavacow:salamander",
            "iceandfire:firedragon",
            "iceandfire:icedragon",
            "iceandfire:lightningdragon"
    };

    @Config.Comment({
            "Whenever Abyssal Rift/Parasite biomes will warn & dismount players that ride into them, on mobs, boats, minecarts or anything else.",
            "Mounts in Allowed Mounts in Abyssal Rift are never dismounted"
    })
    @Config.Name("Dismount Players in Parasite Biomes")
    public boolean abyssalMounts = true;

    @Config.Comment({
            "If true, players can't mount anything while inside Abyssal Rift/Parasite biomes: mobs, boats, minecarts, including the Allowed Mounts.",
            "Players already riding when they enter can keep riding, unless Dismount Players in Parasite Biomes throws them off"
    })
    @Config.Name("Deny Mounting in Parasite Biomes")
    public boolean denyMounting = true;

    @Config.Comment("If true, players can't sleep in beds inside Abyssal Rift/Parasite biomes, so beds there don't set their spawn point either")
    @Config.Name("Deny Sleeping in Parasite Biomes")
    public boolean denySleeping = true;

    @Config.Comment("Parasites spawned in Abyssal Rift will have dmg that is this much higher than other overworld parasites")
    @Config.Name("Abyssal Rift Parasite Stat Multi: Dmg")
    @Config.RequiresMcRestart
    public float abyssalDmgModifier = 2;

    @Config.Comment("Parasites spawned in Abyssal Rift will have health that is this much higher than other overworld parasites")
    @Config.Name("Abyssal Rift Parasite Stat Multi: HP")
    @Config.RequiresMcRestart
    public float abyssalHPModifier = 2;

    @Config.Comment("Parasites spawned in Abyssal Rift will have armor that is this much higher than other overworld parasites")
    @Config.Name("Abyssal Rift Parasite Stat Multi: Armor")
    @Config.RequiresMcRestart
    public float abyssalArmorModifier = 1;

    @Config.Comment({
            "Ticks a player has to keep suffocating in a wall in Abyssal Rift before the wallDmg method teleports them (20 ticks = 1 second).",
            "Getting out of the wall resets the timer. 0 = teleport on the first suffocation damage"
    })
    @Config.Name("Suffocation Teleport Delay")
    @Config.RangeInt(min = 0, max = 6000)
    public int suffocationTpDelay = 60;

    @Config.Comment("Potions and other teleportation methods that will be punished when being applied on a player in Abyssal Rift")
    @Config.Name("Abyssal Rift Teleportation Methods")
    public String[] randomTpPots = {
            "potioncore:teleport",
            "potioncore:strong_teleport",
            "mujmajnkraftsbettersurvival:warp",
            "enderPearl",
            "chorusFruit",
            "enderCrown",
            "enderTalisman"
    };

    public void reset(){
        RandomTpCancelHandler.refreshConfig();
    }
}
