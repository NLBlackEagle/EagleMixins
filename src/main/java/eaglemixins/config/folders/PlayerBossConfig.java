package eaglemixins.config.folders;

import net.minecraft.world.BossInfo;
import net.minecraftforge.common.config.Config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlayerBossConfig {

    @Config.Comment({
            "Bosses that look like players (or a vanilla skeleton/zombie, see Model): a skin, armor, held items and optionally Trinkets and Baubles dragon wings and horns.",
            "With the PLAYER_BOSS AI they fight like skeletons: a bow in the main hand makes them shoot, anything else makes them fight in melee.",
            "Spawn one with its boss id, the key of its entry below, in a spawner or command:",
            "    /summon eaglemixins:player_boss ~ ~ ~ {BossId:\"eagle\"}",
            "    spawner SpawnData: {id:\"eaglemixins:player_boss\",BossId:\"eagle\"}",
            "Champions tiers, Infernal Mobs modifiers and other mods' entity NBT go in Extra NBT, or in the spawner NBT.",
            "Stats, look, flight and drops are re-applied from this config whenever the boss loads.",
            "Equipment and full health are only applied when the boss first spawns.",
            "Items are written as <modid:item>[@damage] [{nbt}], e.g. minecraft:bow@0 {ench:[{id:48s,lvl:5s}]}"
    })
    @Config.Name("Bosses")
    public Map<String, BossEntry> bosses = new LinkedHashMap<>();

    @Config.Comment({
            "If true, infernal player bosses keep their configured Max Health.",
            "FermiumMixins' \"Fix Additional Health (InfernalMobs)\" multiplies infernal health by ability count x Infernal Mobs' mobHealthFactor,",
            "and player bosses only get the one ability from their Extra NBT, so with mobHealthFactor=0.2 they'd only have 20% of it."
    })
    @Config.Name("Ignore Infernal Health Scaling")
    public boolean ignoreInfernalHealth = true;

    public enum FlightMode { NEVER, WHEN_NEEDED, ALWAYS }

    public enum HornStyle { NONE, INVERTED, HORNS, DRAGON }

    /** Model the boss renders with, with the vanilla hitbox (at scale 1) of the mob it copies */
    public enum BossModel {
        PLAYER(0.6F, 1.8F),
        SKELETON(0.6F, 1.99F),
        WITHER_SKELETON(0.7F, 2.4F),
        STRAY(0.6F, 1.99F),
        ZOMBIE(0.6F, 1.95F),
        HUSK(0.6F, 1.95F);

        public final float width;
        public final float height;

        BossModel(float width, float height) {
            this.width = width;
            this.height = height;
        }
    }

    /**
     * PLAYER_BOSS: bow or melee depending on the main hand like skeletons, with optional flight.
     * The others fight the same way but pick their targets like that vanilla mob.
     */
    public enum BossAI { PLAYER_BOSS, ZOMBIE, SKELETON, SPIDER, ZOMBIE_PIGMAN, ENDERMAN, IRON_GOLEM, PASSIVE }

    public static class BossEntry {
        @Config.Comment("Name above the boss and on its boss bar. \u00a7 color codes allowed")
        @Config.Name("Name")
        public String name = "";

        @Config.Comment("Second line under the name, on the boss bar and above the boss. \u00a7 color codes allowed. Empty = none")
        @Config.Name("Subtitle")
        public String subtitle = "";

        @Config.Comment({
                "Model the boss renders with. PLAYER uses Skin and Slim Arms, the others look like that vanilla mob",
                "and also take its hitbox, size and sounds. Wings and horns only show on PLAYER"
        })
        @Config.Name("Model")
        public BossModel model = BossModel.PLAYER;

        @Config.Comment({
                "How the boss picks its targets. Every AI fights with a bow or melee depending on the main hand, with optional flight",
                "PLAYER_BOSS: attacks players on sight, fights back when hit. Spotting or getting hit by a player sends all nearby PLAYER_BOSS bosses after that player too",
                "ZOMBIE: attacks players, villagers and iron golems on sight, calls nearby ZOMBIE bosses for help when hit",
                "SKELETON: attacks players and iron golems on sight, runs from wolves",
                "SPIDER: attacks players and iron golems only in the dark, fights back when hit",
                "ZOMBIE_PIGMAN: neutral until hit, then it and all nearby ZOMBIE_PIGMAN bosses get angry at players for 20-40 seconds",
                "ENDERMAN: neutral until hit or a player looks straight at it (a pumpkin on the head prevents that)",
                "IRON_GOLEM: attacks hostile mobs (except creepers and other player bosses), fights back when hit",
                "PASSIVE: never attacks, panics and runs when hit"
        })
        @Config.Name("AI")
        public BossAI ai = BossAI.PLAYER_BOSS;

        @Config.Comment({
                "Skin: a file name in assets/eaglemixins/textures/entity/player_boss/ without .png (e.g. nlblackeagle),",
                "or a full texture location (modid:textures/entity/skin.png).",
                "The texture has to fit the Model's layout (player skins only fit PLAYER). Empty = Steve, or the vanilla mob's texture"
        })
        @Config.Name("Skin")
        public String skin = "";

        @Config.Comment("Use the slim (Alex) arm model, needed for skins made for slim arms. PLAYER model only")
        @Config.Name("Slim Arms")
        public boolean slimArms = false;

        @Config.Comment("Size multiplier of the model, hitbox and wings. 2 = twice as tall as a player")
        @Config.Name("Scale")
        @Config.RangeDouble(min = 0.25, max = 4)
        public float scale = 1.0F;

        @Config.Name("Max Health")
        @Config.RangeDouble(min = 1)
        public double maxHealth = 200;

        @Config.Name("Armor")
        @Config.RangeDouble(min = 0)
        public double armor = 0;

        @Config.Name("Armor Toughness")
        @Config.RangeDouble(min = 0)
        public double armorToughness = 0;

        @Config.Name("Attack Damage")
        @Config.RangeDouble(min = 0)
        public double attackDamage = 5;

        @Config.Comment({
                "Melee reach in blocks, from the boss' center to the target's center.",
                "0 = vanilla mob reach, which grows with Scale (about 1.2 blocks at scale 1). Also used while flying"
        })
        @Config.Name("Attack Reach")
        @Config.RangeDouble(min = 0, max = 16)
        public double attackReach = 0;

        @Config.Comment({
                "Max damage a single hit from a player (melee, arrows, spells) can deal to this boss, after armor.",
                "Stops very strong gear from one-shotting the boss. 0 = no cap"
        })
        @Config.Name("Player Damage Cap")
        @Config.RangeDouble(min = 0)
        public double playerDamageCap = 0;

        @Config.Comment({
                "Name the boss' main hand weapon in death messages: \"<player> was slain by <boss> using [weapon]\".",
                "Vanilla only does this for renamed items, so the weapon gets its own name as a custom name when equipped"
        })
        @Config.Name("Announce Weapon")
        public boolean announceWeapon = true;

        @Config.Comment({
                "Custom name of the main hand weapon, shown in death messages: \"<player> was slain by <boss> using [Cheese Stick]\".",
                "\u00a7 color codes allowed. Empty = the item's own name"
        })
        @Config.Name("Weapon Name")
        public String weaponName = "";

        @Config.Comment("Walking speed, players walk at 0.1, zombies at 0.23")
        @Config.Name("Movement Speed")
        @Config.RangeDouble(min = 0)
        public double movementSpeed = 0.25;

        @Config.Name("Knockback Resistance")
        @Config.RangeDouble(min = 0, max = 1)
        public double knockbackResistance = 1.0;

        @Config.Comment("How far away the boss notices players")
        @Config.Name("Follow Range")
        @Config.RangeDouble(min = 1, max = 2048)
        public double followRange = 32;

        @Config.Name("Experience")
        @Config.RangeInt(min = 0)
        public int experience = 50;

        @Config.Name("Fire Immune")
        public boolean fireImmune = true;

        @Config.Name("Fall Damage Immune")
        public boolean fallImmune = true;

        @Config.Comment("Ticks between arrows when holding a bow (20 ticks = 1 second)")
        @Config.Name("Bow Attack Interval")
        @Config.RangeInt(min = 1, max = 200)
        public int bowInterval = 20;

        @Config.Comment("Multiplier on the damage of the boss' arrows")
        @Config.Name("Arrow Damage Multiplier")
        @Config.RangeDouble(min = 0)
        public double arrowDamageMultiplier = 1.0;

        @Config.Name("Equipment")
        public Equipment equipment = new Equipment();

        @Config.Comment({
                "Extra entity NBT applied when the boss is created, before it joins the world, like spawner SpawnData.",
                "Use it for other mods' data, e.g. {ForgeCaps:{\"champions:championship\":{tier:4}},ForgeData:{InfernalMobsMod:\"Storm\"}}",
                "NBT given by the spawner or /summon wins over this. Stats, name and equipment come from the fields above. Empty = none"
        })
        @Config.Name("Extra NBT")
        public String extraNbt = "";

        @Config.Name("Boss Bar")
        public BossBar bossBar = new BossBar();

        @Config.Name("Flight")
        public Flight flight = new Flight();

        @Config.Name("Sounds")
        public Sounds sounds = new Sounds();

        @Config.Comment("Trinkets and Baubles dragon wings and horns. Only shown when Trinkets and Baubles (0.33+) is installed")
        @Config.Name("Wings")
        public Wings wings = new Wings();

        @Config.Comment("Loot table rolled on death in addition to the drops below, e.g. dregora:entities/playerbosses/abyssal_tower_developers. Empty = none")
        @Config.Name("Loot Table")
        public String lootTable = "";

        @Config.Comment("Items dropped on death")
        @Config.Name("Drops")
        public List<Drop> drops = new ArrayList<>();

        public BossEntry() {} // needed for betterconfig
    }

    public static class Equipment {
        @Config.Name("Main Hand")
        public String mainhand = "";
        @Config.Name("Off Hand")
        public String offhand = "";
        @Config.Name("Head")
        public String head = "";
        @Config.Name("Chest")
        public String chest = "";
        @Config.Name("Legs")
        public String legs = "";
        @Config.Name("Feet")
        public String feet = "";

        @Config.Comment("Chance for each equipment piece to drop on death")
        @Config.Name("Drop Chance")
        @Config.RangeDouble(min = 0, max = 1)
        public float dropChance = 0;

        public Equipment() {}
    }

    public static class BossBar {
        @Config.Name("Enabled")
        public boolean enabled = true;
        @Config.Name("Color")
        public BossInfo.Color color = BossInfo.Color.PURPLE;
        @Config.Name("Style")
        public BossInfo.Overlay overlay = BossInfo.Overlay.PROGRESS;
        @Config.Name("Darken Sky")
        public boolean darkenSky = false;

        public BossBar() {}
    }

    public static class Flight {
        @Config.Comment({
                "NEVER: walks only. WHEN_NEEDED: takes off when its target is out of reach (higher, lower, or no path). ALWAYS: flies whenever it has a target.",
                "Flying bosses with a bow hover above their target and keep shooting, melee bosses dive at it."
        })
        @Config.Name("Mode")
        public FlightMode mode = FlightMode.WHEN_NEEDED;

        @Config.Comment("Max flying speed in blocks per tick")
        @Config.Name("Speed")
        @Config.RangeDouble(min = 0.05, max = 3)
        public double speed = 0.4;

        @Config.Comment("Blocks above its target a bow boss hovers at")
        @Config.Name("Hover Height")
        @Config.RangeDouble(min = 0, max = 32)
        public double hoverHeight = 5;

        @Config.Comment("Horizontal distance a bow boss keeps from its target while flying")
        @Config.Name("Keep Distance")
        @Config.RangeDouble(min = 0, max = 32)
        public double keepDistance = 8;

        public Flight() {}
    }

    public static class Sounds {
        @Config.Comment({
                "Sound ids, e.g. eaglemixins:entity.developer.idle or minecraft:entity.ghast.scream.",
                "Empty = the Model's own sound for idle, hurt and death (players have no idle sound), and none for target and respond"
        })
        @Config.Name("Idle Sound")
        public String idle = "";

        @Config.Comment("Played once, a moment after the boss spots a player when it had no target before")
        @Config.Name("Target Sound")
        public String target = "";

        @Config.Comment({
                "Played by bosses answering another boss: PLAYER_BOSS bosses alerted by one that spotted a player,",
                "and ZOMBIE bosses called for help. Each one answers after a random 1-3 seconds"
        })
        @Config.Name("Respond Sound")
        public String respond = "";

        @Config.Name("Hurt Sound")
        public String hurt = "";

        @Config.Name("Death Sound")
        public String death = "";

        @Config.Comment("Base pitch of all sounds, each one varies a little around it. Give every boss its own so players can tell them apart")
        @Config.Name("Pitch")
        @Config.RangeDouble(min = 0.5, max = 2)
        public float pitch = 1.0F;

        @Config.Comment("Volume of the target, hurt and death sounds. Above 1 it isn't louder up close, but is heard further away: up to 16 x this many blocks")
        @Config.Name("Volume")
        @Config.RangeDouble(min = 0, max = 16)
        public float volume = 1.0F;

        @Config.Comment("Volume of the idle sound, heard up to 16 x this many blocks away")
        @Config.Name("Idle Volume")
        @Config.RangeDouble(min = 0, max = 16)
        public float idleVolume = 1.0F;

        @Config.Comment("Volume of the respond sound, heard up to 16 x this many blocks away")
        @Config.Name("Respond Volume")
        @Config.RangeDouble(min = 0, max = 16)
        public float respondVolume = 1.0F;

        @Config.Comment("Average seconds between idle sounds, which only play while the boss has no target. Vanilla mobs are about 4")
        @Config.Name("Idle Interval")
        @Config.RangeDouble(min = 1, max = 3600)
        public double idleInterval = 4;

        @Config.Comment("Chance to play the hurt sound when hit. It never plays again within 2.5 seconds, so long hurt sounds don't overlap")
        @Config.Name("Hurt Chance")
        @Config.RangeDouble(min = 0, max = 1)
        public float hurtChance = 1.0F;

        @Config.Comment("Min seconds between two target sounds of this boss, so losing and finding a target again doesn't repeat it")
        @Config.Name("Target Cooldown")
        @Config.RangeDouble(min = 0, max = 3600)
        public double targetCooldown = 30;

        public Sounds() {}
    }

    public static class Wings {
        @Config.Name("Show Wings")
        public boolean enabled = false;
        @Config.Comment("Hex color of the wing bones")
        @Config.Name("Primary Color")
        public String primaryColor = "#FFFFFF";
        @Config.Comment("Hex color of the wing membrane. Same as primary = one-colored wings")
        @Config.Name("Secondary Color")
        public String secondaryColor = "#FFFFFF";
        @Config.Name("Horns")
        public HornStyle horns = HornStyle.NONE;
        @Config.Name("Horn Color")
        public String hornColor = "#FFFFFF";

        public Wings() {}
    }

    public static class Drop {
        @Config.Comment("<modid:item>[@damage] [{nbt}], e.g. eaglemixins:lock_key {LockId:\"brutal_dev\"}")
        @Config.Name("Item")
        public String item = "";
        @Config.Name("Min Count")
        @Config.RangeInt(min = 0)
        public int min = 1;
        @Config.Name("Max Count")
        @Config.RangeInt(min = 0)
        public int max = 1;
        @Config.Name("Chance")
        @Config.RangeDouble(min = 0, max = 1)
        public float chance = 1;
        @Config.Comment("Extra chance per looting level")
        @Config.Name("Looting Bonus")
        @Config.RangeDouble(min = 0, max = 1)
        public float lootingBonus = 0;

        public Drop() {}

        public Drop(String item, int min, int max, float chance) {
            this.item = item;
            this.min = min;
            this.max = max;
            this.chance = chance;
        }
    }

    public PlayerBossConfig() {
        PlayerBossDefaults.addDefaults(bosses);
    }
}
