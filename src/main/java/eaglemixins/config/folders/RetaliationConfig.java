package eaglemixins.config.folders;

import eaglemixins.EagleMixins;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RetaliationConfig {

    @Config.Comment("Master toggle. If false, no mob gets the retaliation AI and kills don't alert nearby mobs.")
    @Config.Name("Enabled")
    public boolean enabled = true;

    @Config.Comment({
            "Mobs that fight back. One mob per line: <target>, key=value, ...",
            "<target> is an entity id (minecraft:cow) or @modid for every mob of that mod (@familiarfauna). An entity id wins over its @modid line.",
            "  damage  Damage per hit for mobs that get the EagleMixins melee attack (mobs without a melee attack of their own)",
            "  speed   Chase speed multiplier, on top of the mob's movement speed. Villagers move fast by default, so they need a lower value",
            "Settings left out use \"Default Damage\" and \"Default Speed\".",
            "Vanilla style mobs (animals, villagers, most modded animals) fight back when hit and call nearby mobs of the same kind for help.",
            "Lycanites mobs keep their own AI and creature group (config/lycanitesmobs/creaturegroups). Listed ones fight back against",
            "players (not creative) and call nearby mobs for help, even when their group is \"wary\". Against other mobs their group still",
            "decides, so e.g. animals keep running from predators. Needs the \"Retaliation (LycanitesMobs)\" mixin toggle.",
            "Damage and speed don't apply to Lycanites mobs, they use their own attack.",
            "Changes only apply to mobs that (re)join the world, e.g. after a relog or chunk reload."
    })
    @Config.Name("Retaliating Mobs")
    public String[] rules = {
            "minecraft:cow",
            "minecraft:mooshroom",
            "minecraft:sheep",
            "minecraft:pig",
            "minecraft:chicken, damage=1",
            "minecraft:rabbit, damage=1",
            "minecraft:horse, damage=3",
            "minecraft:donkey, damage=3",
            "minecraft:mule, damage=3",
            "minecraft:skeleton_horse, damage=3",
            "minecraft:zombie_horse, damage=3",
            "minecraft:parrot, damage=1",
            "minecraft:ocelot",
            "minecraft:villager, speed=0.7",
            "iceandfire:snowvillager, speed=0.7",
            "quark:archaeologist, speed=0.7",
            "quark:crab",
            "familiarfauna:familiarfauna.deer, damage=3",
            "familiarfauna:familiarfauna.turkey, damage=1",
            "mod_lavacow:lavacow",
            "mod_lavacow:raven, damage=1",
            "iceandfire:hippogryph",
            "iceandfire:hippocampus",
            "lycanitesmobs:yale",
            "lycanitesmobs:maka",
            "lycanitesmobs:makaalpha",
            "lycanitesmobs:arisaur",
            "lycanitesmobs:joust",
            "lycanitesmobs:joustalpha",
            "lycanitesmobs:aspid",
            "lycanitesmobs:bobeko",
            "lycanitesmobs:ika",
            "lycanitesmobs:silex",
            "lycanitesmobs:cephignis",
            "lycanitesmobs:conba",
            "lycanitesmobs:concapede",
            "lycanitesmobs:krake",
            "lycanitesmobs:pinky",
            "lycanitesmobs:calpod",
            "lycanitesmobs:gorgomite",
            "lycanitesmobs:kobold",
            "lycanitesmobs:quillbeast"
    };

    @Config.Comment("Damage per hit for listed mobs without a damage setting")
    @Config.Name("Default Damage")
    @Config.RangeDouble(min = 0)
    public double defaultDamage = 1.0D;

    @Config.Comment("Chase speed multiplier for listed mobs without a speed setting")
    @Config.Name("Default Speed")
    @Config.RangeDouble(min = 0)
    public double defaultSpeed = 1.0D;

    @Config.Comment("Ticks between two hits of the EagleMixins melee attack (20 ticks = 1 second)")
    @Config.Name("Attack Cooldown Ticks")
    @Config.RangeInt(min = 1)
    public int attackCooldown = 20;

    @Config.Comment({
            "AI task priority of the melee attack, lower = more important.",
            "Vanilla animals: 0 swim, 1 panic, 2 mate, 3 tempt, 4 follow parent, 5 wander. Villagers: 1 avoid zombies/trade, 2 move indoors.",
            "Panicking is skipped while the mob has someone to fight, so equal priorities are fine."
    })
    @Config.Name("Attack AI Priority")
    @Config.RangeInt(min = 0)
    public int attackPriority = 1;

    @Config.Comment("If false, babies keep running away instead of fighting back")
    @Config.Name("Babies Fight Back")
    public boolean babiesFightBack = false;

    @Config.Comment({
            "When a listed mob is killed, mobs of the same kind within this many blocks go after the killer.",
            "This also works when the mob was killed in one hit, which never makes it fight back itself. 0 = off"
    })
    @Config.Name("Kill Alert Radius")
    @Config.RangeDouble(min = 0)
    public double killAlertRadius = 16.0D;

    @Config.Comment("If true, a kill alerts every listed mob in range instead of only mobs of the same kind (e.g. killing a cow makes nearby sheep attack too)")
    @Config.Name("Kill Alerts Any Listed Mob")
    public boolean killAlertsAnyListed = false;

    public static class Settings {
        public final double damage;
        public final double speed;

        public Settings(double damage, double speed) {
            this.damage = damage;
            this.speed = speed;
        }
    }

    private Map<ResourceLocation, Settings> byEntity = null;
    private Map<String, Settings> byMod = null;

    public void reset() {
        byEntity = null;
        byMod = null;
    }

    /** Settings of the rule matching this mob (entity id first, then @modid), null if it isn't listed */
    @Nullable
    public Settings get(Entity entity) {
        ResourceLocation id = EntityList.getKey(entity);
        return id == null ? null : get(id);
    }

    @Nullable
    public Settings get(ResourceLocation id) {
        if (byEntity == null || byMod == null) parse();
        Settings settings = byEntity.get(id);
        return settings != null ? settings : byMod.get(id.getNamespace());
    }

    private void parse() {
        Map<ResourceLocation, Settings> newByEntity = new LinkedHashMap<>();
        Map<String, Settings> newByMod = new HashMap<>();

        for (String line : rules) {
            String[] parts = line.split(",");
            String target = parts[0].trim();
            if (target.isEmpty()) continue;

            double damage = defaultDamage;
            double speed = defaultSpeed;
            for (int i = 1; i < parts.length; i++) {
                String[] kv = parts[i].split("=", 2);
                if (kv.length != 2) {
                    EagleMixins.LOGGER.warn("Retaliation: ignoring \"{}\" in \"{}\", expected key=value", parts[i].trim(), line);
                    continue;
                }
                String key = kv[0].trim();
                try {
                    double value = Double.parseDouble(kv[1].trim());
                    if (key.equals("damage")) damage = value;
                    else if (key.equals("speed")) speed = value;
                    else EagleMixins.LOGGER.warn("Retaliation: unknown setting \"{}\" in \"{}\"", key, line);
                } catch (NumberFormatException e) {
                    EagleMixins.LOGGER.warn("Retaliation: \"{}\" is not a number in \"{}\"", kv[1].trim(), line);
                }
            }

            Settings settings = new Settings(damage, speed);
            if (target.startsWith("@")) {
                String modid = target.substring(1).trim();
                if (modid.isEmpty()) EagleMixins.LOGGER.warn("Retaliation: missing modid in \"{}\"", line);
                else newByMod.put(modid, settings);
            } else {
                newByEntity.put(new ResourceLocation(target), settings);
            }
        }

        byEntity = Collections.unmodifiableMap(newByEntity);
        byMod = Collections.unmodifiableMap(newByMod);
    }
}
