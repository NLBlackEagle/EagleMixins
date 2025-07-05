package eaglemixins.config.folders;

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
            "minecraft:llama"
    };

    @Config.Comment("Whenever Abyssal Rift/Parasite biomes will warn & dismount players")
    @Config.Name("Dismount Players in Parasite Biomes")
    public boolean abyssalMounts = false;

    @Config.Comment("Parasites spawned in Abyssal Rift will have dmg that is this much higher than other overworld parasites")
    @Config.Name("Abyssal Rift Parasite Stat Multi: Dmg")
    @Config.RequiresMcRestart
    public float abyssalDmgModifier = 1;

    @Config.Comment("Parasites spawned in Abyssal Rift will have health that is this much higher than other overworld parasites")
    @Config.Name("Abyssal Rift Parasite Stat Multi: HP")
    @Config.RequiresMcRestart
    public float abyssalHPModifier = 1;

    @Config.Comment("Parasites spawned in Abyssal Rift will have armor that is this much higher than other overworld parasites")
    @Config.Name("Abyssal Rift Parasite Stat Multi: Armor")
    @Config.RequiresMcRestart
    public float abyssalArmorModifier = 1;
}
