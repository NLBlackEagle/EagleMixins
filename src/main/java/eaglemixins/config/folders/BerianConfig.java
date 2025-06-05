package eaglemixins.config.folders;

import eaglemixins.EagleMixins;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;

public class BerianConfig {

    @Config.Comment("Chance of a librarian being converted to a Sussyberian on generation")
    @Config.Name("Sussyberian Chance")
    @Config.RangeDouble(min = 0D, max = 1D)
    public double sussyberianChance = 0.05D;

    @Config.Comment("Chance of a librarian being converted to a Mentalberian on generation")
    @Config.Name("Mentalberian Chance")
    @Config.RangeDouble(min = 0D, max = 1D)
    public double mentalberianChance = 0.05D;

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

    private List<Potion> sussyberianEffectList = null;
    private List<Potion> mentalberianEffectList = null;
    private Potion berianConstantEffectPotion = null;

    public List<Potion> getSussyberianEffects() {
        if(sussyberianEffectList == null) {
            sussyberianEffectList = new ArrayList<>();
            for(String name : this.sussyberianEffects) {
                if(name.trim().isEmpty()) continue;
                Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(name.trim()));
                if(potion == null) {
                    EagleMixins.LOGGER.log(Level.WARN, "Unable to find potion effect: " + name);
                    continue;
                }
                sussyberianEffectList.add(potion);
            }
        }
        return sussyberianEffectList;
    }

    public List<Potion> getMentalberianEffects() {
        if(mentalberianEffectList == null) {
            mentalberianEffectList = new ArrayList<>();
            for(String name : this.mentalberianEffects) {
                if(name.trim().isEmpty()) continue;
                Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(name.trim()));
                if(potion == null) {
                    EagleMixins.LOGGER.log(Level.WARN, "Unable to find potion effect: " + name);
                    continue;
                }
                mentalberianEffectList.add(potion);
            }
        }
        return mentalberianEffectList;
    }

    public Potion getBerianConstantEffect() {
        if(berianConstantEffectPotion == null) {
            String name = this.berianConstantEffect.trim();
            Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(name));
            if(potion == null) {
                EagleMixins.LOGGER.log(Level.WARN, "Unable to find potion effect: " + name);
                berianConstantEffectPotion = MobEffects.NAUSEA;
            }
            else berianConstantEffectPotion = potion;
        }
        return berianConstantEffectPotion;
    }

    public void reset(){
        mentalberianEffects = null;
        sussyberianEffects = null;
        berianConstantEffect = null;
    }
}
