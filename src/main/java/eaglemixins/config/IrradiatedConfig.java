package eaglemixins.config;

import net.minecraftforge.common.config.Config;

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
}
