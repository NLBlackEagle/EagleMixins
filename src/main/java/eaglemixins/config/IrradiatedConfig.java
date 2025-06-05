package eaglemixins.config;

import net.minecraftforge.common.config.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public String[] irradiatedEntities = {
            "srparasites:*"
    };

    @Config.Comment("Make the irradiated entities whitelist act as a blacklist, so all entitylivingbase will be affected except for the config named ones.")
    @Config.Name("Irradiated Entities Whitelist is Blacklist")
    public boolean irradiatedEntitiesIsBlacklist = false;

    private final List<String> irradiatedEntityList = new ArrayList<>();

    public List<String> getIrradiatedEntityList(){
        if(irradiatedEntityList.isEmpty() && irradiatedEntities.length > 0)
            irradiatedEntityList.addAll(Arrays.asList(this.irradiatedEntities));
        return irradiatedEntityList;
    }

    public void reset(){
        irradiatedEntityList.clear();
    }
}
