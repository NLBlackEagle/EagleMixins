package eaglemixins.config.folders;

import net.minecraftforge.common.config.Config;

public class WolfPackConfig {

    @Config.Comment({
            "Master toggle for wolf packs. Every wild pack gets a fixed alpha wolf (stored on the wolf, so it stays the alpha).",
            "The alpha decides when the pack hunts a player, nearby wolves join in.",
            "While enabled, a \"minecraft:wolf\" entry in \"Angry Mobs\" is ignored."
    })
    @Config.Name("Enabled")
    public boolean enabled = true;

    // ---------- Alpha ----------

    @Config.Comment({
            "A wild adult wolf that enters the world for the first time, with no alpha within \"Alpha Search Radius\",",
            "becomes an alpha with this chance. Otherwise it joins a nearby alpha with room in its pack, or stays on its own.",
            "Existing wolves are checked once on their next load. 1 = always"
    })
    @Config.Name("Alpha Chance")
    @Config.RangeDouble(min = 0, max = 1)
    public double alphaChance = 0.1D;

    @Config.Comment("New wolves only roll \"Alpha Chance\" when no other alpha is within this many blocks, so a pack gets one alpha")
    @Config.Name("Alpha Search Radius")
    @Config.RangeDouble(min = 1)
    public double alphaSearchRadius = 32.0D;

    @Config.Comment({
            "Renders alpha wolves with their own skin (client side): eaglemixins:textures/entity/wolf/alpha_wolf.png",
            "and alpha_wolf_angry.png while hunting. Needs the \"Alpha Wolf Skin (Vanilla)\" mixin toggle"
    })
    @Config.Name("Alpha Custom Skin")
    public boolean alphaCustomSkin = true;

    @Config.Comment("Max health multiplier of alpha wolves (vanilla wild wolf: 8 hp)")
    @Config.Name("Alpha Health Multiplier")
    @Config.RangeDouble(min = 0.05)
    public double alphaHealthMultiplier = 2.0D;

    @Config.Comment({
            "Extra movement speed of angry wild wolves (alphas included) while their player target is further away than",
            "\"Angry Speed Min Distance\". 0.15 = 15% faster. Not angry, or closer than that = normal speed"
    })
    @Config.Name("Angry Speed Bonus")
    @Config.RangeDouble(min = 0)
    public double angrySpeedBonus = 0.15D;

    @Config.Comment("Angry wolves only get \"Angry Speed Bonus\" while their player target is further away than this many blocks")
    @Config.Name("Angry Speed Min Distance")
    @Config.RangeDouble(min = 0)
    public double angrySpeedMinDistance = 5.0D;

    @Config.Comment({
            "Name of alpha wolves, e.g. in death messages (\"was slain by Alpha Wolf\") and health bars. Empty = plain \"Wolf\".",
            "Needs the \"Alpha Wolf Name (Vanilla)\" mixin toggle"
    })
    @Config.Name("Alpha Name")
    public String alphaName = "Alpha Wolf";

    @Config.Comment({
            "How far away alpha wolves notice players and keep chasing them, in blocks (vanilla wolf: 16).",
            "Wolves give up on a target further away than their follow range"
    })
    @Config.Name("Alpha Follow Range")
    @Config.RangeDouble(min = 1)
    public double alphaFollowRange = 32.0D;

    @Config.Comment({
            "Follow range of pack members, in blocks (vanilla wolf: 16). Wolves without an alpha keep the vanilla range. It also limits how far they can path to a target,",
            "so keep it at least \"Alpha Follow Range\": an alpha calls its pack for help from that far, and wolves that can't path to the player just stand there"
    })
    @Config.Name("Pack Member Follow Range")
    @Config.RangeDouble(min = 1)
    public double memberFollowRange = 32.0D;

    @Config.Comment({
            "Damage multiplier of alpha wolf attacks against anything (vanilla wild wolf: 2 damage).",
            "Against players \"Damage To Players Multiplier\" is applied on top"
    })
    @Config.Name("Alpha Damage Multiplier")
    @Config.RangeDouble(min = 0)
    public double alphaDamageMultiplier = 1.25D;

    // ---------- Hunting ----------

    @Config.Comment({
            "Chance that the alpha starts hunting a player that comes into view, rolled once per sighting.",
            "A miss is rolled again the next time the player comes into view"
    })
    @Config.Name("Hunt Chance Per Sighting")
    @Config.RangeDouble(min = 0, max = 1)
    public float huntChance = 1.0F;

    @Config.Comment({
            "Pack members within this many blocks of their alpha join its stalking and attacks.",
            "Wolves without a pack join an alpha with room in its pack (\"Max Pack Size\") within this many blocks"
    })
    @Config.Name("Pack Radius")
    @Config.RangeDouble(min = 0)
    public double packRadius = 16.0D;

    @Config.Comment("Most wolves in one pack, the alpha included. Wolves that don't fit stay on their own")
    @Config.Name("Max Pack Size")
    @Config.RangeInt(min = 1)
    public int maxPackSize = 6;

    @Config.Comment("Pack members walk back to their alpha when they wander further away than this many blocks")
    @Config.Name("Pack Follow Distance")
    @Config.RangeDouble(min = 2)
    public double packFollowDistance = 10.0D;

    // ---------- Alpha rivalry ----------

    @Config.Comment({
            "Two alphas that see each other fight, instead of hunting players. Other wolves stay out of it.",
            "The loser flees (and stays an alpha, on its own), its wolves join the winner's pack as far as \"Max Pack Size\" allows"
    })
    @Config.Name("Alpha Rivalry")
    public boolean alphaRivalry = true;

    @Config.Comment("Alphas fight when another alpha is within this many blocks and in sight")
    @Config.Name("Alpha Rivalry Radius")
    @Config.RangeDouble(min = 1)
    public double rivalryRadius = 16.0D;

    @Config.Comment("An alpha gives up the fight and flees when its health drops to this fraction of its max health")
    @Config.Name("Alpha Rivalry Flee Health")
    @Config.RangeDouble(min = 0, max = 1)
    public double rivalryFleeHealth = 0.25D;

    @Config.Comment("Seconds the losing alpha runs away from the winner. Wolves can't hurt it meanwhile")
    @Config.Name("Alpha Rivalry Flee Seconds")
    @Config.RangeInt(min = 0)
    public int rivalryFleeSeconds = 15;

    @Config.Comment("Seconds after a fight before either alpha fights another alpha again")
    @Config.Name("Alpha Rivalry Cooldown Seconds")
    @Config.RangeInt(min = 0)
    public int rivalryCooldownSeconds = 180;

    @Config.Comment({
            "When wolves start hunting players that come into view (\"Hunt Chance Per Sighting\" is still rolled):",
            "  0 = always",
            "  1 = at night (world time 12000-24000)",
            "  2 = when it's cold: Simple Difficulty temperature at the wolf at or below \"Cold Temperature\" (never without Simple Difficulty)",
            "  3 = at night or when it's cold"
    })
    @Config.Name("Hunt Condition")
    @Config.RangeInt(min = 0, max = 3)
    public int huntCondition = 0;

    @Config.Comment("Simple Difficulty temperature (0-25) that counts as cold. 0-5 freezing, 6-10 cold, 11-14 normal, 15-19 hot, 20-25 burning")
    @Config.Name("Cold Temperature")
    @Config.RangeInt(min = 0, max = 25)
    public int coldTemperature = 10;

    @Config.Comment({
            "Before attacking, the wolf that started the hunt (and its pack) stalks the player for this many seconds,",
            "keeping \"Stalk Distance\" blocks away. Attacking the wolf ends the stalking right away. 0 = attack immediately"
    })
    @Config.Name("Stalk Seconds")
    @Config.RangeInt(min = 0)
    public int stalkSeconds = 10;

    @Config.Comment({
            "Seconds an alpha remembers the player it attacked, counted from when it stopped attacking.",
            "Seeing that player again within that time skips the stalking (and the hunt roll): the pack attacks right away. 0 = off"
    })
    @Config.Name("Remember Target Seconds")
    @Config.RangeInt(min = 0)
    public int rememberTargetSeconds = 60;

    @Config.Comment("Distance in blocks stalking wolves keep from the player")
    @Config.Name("Stalk Distance")
    @Config.RangeDouble(min = 1)
    public double stalkDistance = 8.0D;

    @Config.Comment("Movement speed multiplier while stalking")
    @Config.Name("Stalk Speed")
    @Config.RangeDouble(min = 0)
    public double stalkSpeed = 1.0D;

    @Config.Comment("Stalking is called off when the player gets further away than this many blocks")
    @Config.Name("Stalk Give Up Distance")
    @Config.RangeDouble(min = 1)
    public double stalkGiveUpDistance = 32.0D;

    @Config.Comment("Players holding one of these items (either hand) are not hunted, and a hunting wolf calms down when the player pulls one out")
    @Config.Name("Calming Items")
    public String[] calmingItems = {"minecraft:bone"};

    @Config.Comment("Ticks a wolf keeps hunting after losing line of sight of its target before it calms down (20 ticks = 1 second)")
    @Config.Name("Lose Sight Grace Ticks")
    @Config.RangeInt(min = 0)
    public int loseSightTicks = 60;

    @Config.Comment({
            "Where the hunting goes in the wolf's target task list. Lower = more important.",
            "Vanilla wolf: 1-2 defend/help owner, 3 fight back when hurt, 4 hunt sheep/rabbits, 5 attack skeletons"
    })
    @Config.Name("AI Task Priority")
    @Config.RangeInt(min = 0)
    public int aiPriority = 4;

    // ---------- Damage ----------

    @Config.Comment({
            "Damage multiplier of wild (untamed) wolf attacks against players, alphas included.",
            "Vanilla wild wolf: 2 damage (Easy 2, Normal 2, Hard 3), so 0.5 = half a heart on Normal"
    })
    @Config.Name("Damage To Players Multiplier")
    @Config.RangeDouble(min = 0)
    public double playerDamageMultiplier = 0.5D;

    @Config.Comment({
            "First Aid only aims melee hits of monsters (EntityMob). Wolves are animals, so their bites hit a random body part,",
            "head included, which can kill in one bite when the head is already hurt.",
            "If true, bite damage that lands on a part not listed in \"First Aid: Wolf Bite Parts\" is moved to the listed parts instead"
    })
    @Config.Name("First Aid: Redirect Wolf Bites")
    public boolean firstAidRedirectBites = true;

    @Config.Comment("Body parts wild wolf bites can hit with First Aid. HEAD, BODY, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG, LEFT_FOOT, RIGHT_FOOT")
    @Config.Name("First Aid: Wolf Bite Parts")
    public String[] firstAidBiteParts = {"LEFT_LEG", "RIGHT_LEG", "LEFT_FOOT", "RIGHT_FOOT", "LEFT_ARM", "RIGHT_ARM", "BODY"};
}
