package eaglemixins.config.folders;

import eaglemixins.EagleMixins;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class TippedArrowConfig {
    @Config.Comment("Chance for an entity to have its arrow replaced with a tipped arrow")
    @Config.Name("Tipped Arrow Replacement Chance")
    @Config.RangeDouble(min = 0.0F, max = 1.0F)
    public float tippedArrowReplacementChance = 0.05F;

    @Config.Comment("List of entities to allow randomly adding tipped arrows")
    @Config.Name("Tipped Arrow Replacement Allowed Entities")
    public String[] tippedArrowEntities = {
            "minecraft:skeleton",
            "minecraft:wither_skeleton"
    };

    @Config.Comment("List of long potion types to be used for tipped arrows randomly added to entities")
    @Config.Name("Tipped Arrow Replacement Allowed PotionTypes Long")
    public String[] tippedArrowPotionsLong = {
            "potioncore:long_klutz",
            "potioncore:long_weight",
            "potioncore:long_broken_armor",
            "potioncore:long_spin",
            "quark:long_mining_fatigue",
            "potioncore:long_magic_inhibition",
            "potioncore:long_drown",
            "potioncore:long_vulnerable",
            "potioncore:long_rust",
            "potioncore:long_perplexity",
            "minecraft:long_slowness",
            "mujmajnkraftsbettersurvival:long_antiwarp",
            "mujmajnkraftsbettersurvival:long_decay",
            "mujmajnkraftsbettersurvival:long_blindness",
            "potioncore:long_blindness",
            "potioncore:long_nausea",
            "potioncore:long_levitation",
            "potioncore:long_hunger",
            "potioncore:long_wither",
            "elenaidodge:long_sluggish",
            "minecraft:long_poison",
            "minecraft:long_weakness",
            "elenaidodge:long_feeble",
            "potioncore:long_broken_magic_shield"
    };

    @Config.Comment("List of non-long potion types to be used for tipped arrows randomly added to entities")
    @Config.Name("Tipped Arrow Replacement Allowed PotionTypes Short")
    public String[] tippedArrowPotions = {
            "potioncore:strong_broken_armor",
            "potioncore:broken_armor",
            "potioncore:strong_klutz",
            "potioncore:klutz",
            "potioncore:dispel",
            "potioncore:strong_launch",
            "potioncore:launch",
            "potioncore:spin",
            "potioncore:strong_spin",
            "potioncore:curse",
            "potioncore:strong_curse",
            "quark:mining_fatigue",
            "quark:strong_mining_fatigue",
            "potioncore:disorganization",
            "srparasites:foster",
            "srparasites:coth",
            "srparasites:fear",
            "srparasites:res",
            "srparasites:corro",
            "srparasites:vira",
            "srparasites:rage",
            "srparasites:debar",
            "potioncore:magic_inhibition",
            "potioncore:weight",
            "potioncore:lightning",
            "potioncore:strong_explode",
            "potioncore:teleport",
            "potioncore:strong_teleport",
            "potioncore:teleport_surface",
            "potioncore:drown",
            "potioncore:teleport_spawn",
            "potioncore:strong_vulnerable",
            "potioncore:vulnerable",
            "potioncore:strong_rust",
            "potioncore:rust",
            "potioncore:perplexity",
            "minecraft:slowness",
            "mujmajnkraftsbettersurvival:milk",
            "mujmajnkraftsbettersurvival:antiwarp",
            "mujmajnkraftsbettersurvival:strong_decay",
            "mujmajnkraftsbettersurvival:decay",
            "potioncore:nausea",
            "potioncore:levitation",
            "potioncore:strong_levitation",
            "potioncore:unluck",
            "potioncore:strong_hunger",
            "potioncore:hunger",
            "potioncore:strong_wither",
            "potioncore:wither",
            "elenaidodge:sluggish",
            "elenaidodge:strong_feeble",
            "minecraft:harming",
            "minecraft:strong_harming",
            "minecraft:poison",
            "minecraft:strong_poison",
            "mujmajnkraftsbettersurvival:blindness",
            "elenaidodge:feeble",
            "potioncore:strong_magic_inhibition",
            "potioncore:strong_weight",
            "potioncore:fire",
            "potioncore:invert",
            "potioncore:broken_magic_shield",
            "potioncore:strong_broken_magic_shield",
            "potioncore:strong_blindness",
            "potioncore:blindness",
            "potioncore:explode",
            "xat:extended_goblin",
            "xat:goblin"
    };

    private HashSet<ResourceLocation> tippedArrowAllowedEntities = null;
    public HashSet<ResourceLocation> getTippedArrowAllowedEntities() {
        if(tippedArrowAllowedEntities == null) {
            HashSet<ResourceLocation> set = new HashSet<>();
            for(String entity : this.tippedArrowEntities) {
                set.add(new ResourceLocation(entity));
            }
            tippedArrowAllowedEntities = set;
        }
        return tippedArrowAllowedEntities;
    }

    private List<ItemStack> tippedArrowArrayLong = null;
    private List<ItemStack> tippedArrowArray = null;
    public ItemStack getRandomArrowStack(Random rand, boolean isLong){
        //Lazy loading
        if(isLong && tippedArrowArrayLong == null){
            tippedArrowArrayLong = new ArrayList<>();
            for(String potionString : this.tippedArrowPotionsLong) {
                PotionType type = PotionType.getPotionTypeForName(potionString);
                if(type == null){
                    EagleMixins.LOGGER.warn("RLMixins Dregora Arrow PotionTypes invalid PotionType: " + potionString + ", ignoring.");
                    continue;
                }
                tippedArrowArrayLong.add(PotionUtils.addPotionToItemStack(new ItemStack(Items.TIPPED_ARROW),type));
            }
        }
        if(!isLong && tippedArrowArray == null){
            tippedArrowArray = new ArrayList<>();
            for(String potionString : this.tippedArrowPotions) {
                PotionType type = PotionType.getPotionTypeForName(potionString);
                if(type == null){
                    EagleMixins.LOGGER.warn("RLMixins Dregora Arrow PotionTypes invalid PotionType: " + potionString + ", ignoring.");
                    continue;
                }
                tippedArrowArray.add(PotionUtils.addPotionToItemStack(new ItemStack(Items.TIPPED_ARROW),type));
            }
        }
        if(isLong) return tippedArrowArrayLong.get(rand.nextInt(tippedArrowArrayLong.size())).copy();
        else       return tippedArrowArray.get(rand.nextInt(tippedArrowArray.size())).copy();
    }

    public void reset(){
        tippedArrowAllowedEntities = null;
    }
}
