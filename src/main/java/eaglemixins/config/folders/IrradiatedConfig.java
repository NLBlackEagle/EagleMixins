package eaglemixins.config.folders;

import net.minecraftforge.common.config.Config;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class IrradiatedConfig {
    @Config.Comment("Set this to false to fully disable the effect of parasites getting their stats reduced due to getting irradiated.")
    @Config.Name("Enable Irradiated Parasites")
    public boolean enabled = true;

    @Config.Comment("How much % of hp per 0.01 radiation level is removed from parasites")
    @Config.Name("HP Reduction Multiplier")
    public double hpMultiplier = 0.0;

    @Config.Comment("How much % of hp can be maximally removed from parasites due to being irradiated")
    @Config.Name("HP Reduction Upper Limit")
    public double hpUpperLimit = 0.0;

    @Config.Comment("How much % of dmg per 0.01 radiation level is removed from parasites")
    @Config.Name("Damage Reduction Multiplier")
    public double dmgMultiplier = 0.0;

    @Config.Comment("How much % of dmg can be maximally removed from parasites due to being irradiated")
    @Config.Name("Damage Reduction Upper Limit")
    public double dmgUpperLimit = 0.0;

    @Config.Comment("How much % of armor per 0.01 radiation level is removed from parasites")
    @Config.Name("Armor Reduction Multiplier")
    public double armorMultiplier = 0.0;

    @Config.Comment("How much % of armor can be maximally removed from parasites due to being irradiated")
    @Config.Name("Armor Reduction Upper Limit")
    public double armorUpperLimit = 0.0;

    @Config.Comment("What kind of entities will be affected by the irradiation stat reduction. Use modid:* to have all entities(livingbase) of that mob be affected.")
    @Config.Name("Irradiated Entities Whitelist")
    public Set<String> irradiatedEntities = new LinkedHashSet<>(Collections.singletonList("srparasites:*"));

    @Config.Comment("Make the irradiated entities whitelist act as a blacklist, so all entitylivingbase will be affected except for the config named ones.")
    @Config.Name("Irradiated Entities Whitelist is Blacklist")
    public boolean irradiatedEntitiesIsBlacklist = false;
}
