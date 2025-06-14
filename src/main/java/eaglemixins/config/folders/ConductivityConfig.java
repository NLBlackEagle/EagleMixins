package eaglemixins.config.folders;

import eaglemixins.EagleMixins;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Config;

import java.util.HashMap;
import java.util.Map;

public class ConductivityConfig {
    @Config.Comment("List of item ids and their conductivity, separated by a space. For metadata use mod:itemid:metadata conductivity")
    @Config.Name("Item Conductivity")
    public String[] itemConductivities = {
            "mujmajnkraftsbettersurvival:itemironspear 1",
            "mujmajnkraftsbettersurvival:itemirondagger 1",
            "mujmajnkraftsbettersurvival:itemironbattleaxe 1",
            "mujmajnkraftsbettersurvival:itemironnunchaku 1",
            "mujmajnkraftsbettersurvival:itemsteelhammer 1",
            "mujmajnkraftsbettersurvival:itemsteelspear 1",
            "mujmajnkraftsbettersurvival:itemsteeldagger 1",
            "mujmajnkraftsbettersurvival:itemsteelbattleaxe 1",
            "mujmajnkraftsbettersurvival:itemsteelnunchaku 1",
            "minecraft:iron_shovel 1",
            "minecraft:iron_pickaxe 1",
            "minecraft:iron_axe 1",
            "minecraft:flint_and_steel 1",
            "minecraft:iron_sword 1",
            "minecraft:iron_hoe 1",
            "minecraft:chainmail_helmet 1",
            "minecraft:chainmail_chestplate 1",
            "minecraft:chainmail_leggings 1",
            "minecraft:chainmail_boots 1",
            "minecraft:iron_helmet 1",
            "minecraft:iron_chestplate 1",
            "minecraft:iron_leggings 1",
            "minecraft:iron_boots 1",
            "minecraft:bucket 1",
            "minecraft:water_bucket 1",
            "minecraft:lava_bucket 1",
            "minecraft:milk_bucket 1",
            "minecraft:compass 1",
            "minecraft:shears 1",
            "minecraft:shield 1",
            "mujmajnkraftsbettersurvival:itemcrossbow 1",
            "mujmajnkraftsbettersurvival:itemsmallshield 1",
            "mujmajnkraftsbettersurvival:itembigshield 1",
            "mujmajnkraftsbettersurvival:itemironhammer 1",
            "charm:iron_lantern 1",
            "forge:bucketfilled 1",
            "fishingmadebetter:fishing_rod_iron 1",
            "fishingmadebetter:fillet_knife_iron 1",
            "fishingmadebetter:scaling_knife_iron 1",
            "fishingmadebetter:fish_tracker_iron 1",
            "forgottenitems:bound_pickaxe 1",
            "forgottenitems:bound_axe 1",
            "forgottenitems:bound_shovel 1",
            "grapplemod:grapplinghook 1",
            "iceandfire:dread_sword 1",
            "iceandfire:dread_knight_sword 1",
            "iceandfire:troll_weapon.axe 1",
            "iceandfire:troll_weapon.hammer 1",
            "locks:iron_lock_pick 1",
            "locks:steel_lock_pick 1",
            "notreepunching:knife/iron 1",
            "notreepunching:mattock/iron 1",
            "notreepunching:saw/iron 1",
            "rlmixins:steel_helmet 1",
            "rlmixins:steel_chestplate 1",
            "rlmixins:steel_leggings 1",
            "rlmixins:steel_boots 1",
            "rustic:candle 1",
            "rustic:candle_lever 1",
            "rustic:iron_lantern 1",
            "simpledifficulty:iron_canteen 1",
            "spartanshields:shield_basic_iron 1",
            "spartanshields:shield_tower_iron 1",
            "spartanshields:shield_basic_steel 1",
            "spartanshields:shield_tower_steel 1",
            "spartanweaponry:dagger_iron 1",
            "spartanweaponry:longsword_iron 1",
            "spartanweaponry:katana_iron 1",
            "spartanweaponry:scythe_iron 1",
            "spartanweaponry:saber_iron 1",
            "spartanweaponry:rapier_iron 1",
            "spartanweaponry:greatsword_iron 1",
            "spartanweaponry:caestus_studded 1",
            "spartanweaponry:hammer_iron 1",
            "spartanweaponry:warhammer_iron 1",
            "spartanweaponry:spear_iron 1",
            "spartanweaponry:halberd_iron 1",
            "spartanweaponry:throwing_knife_steel 1",
            "spartanweaponry:throwing_axe_steel 1",
            "spartanweaponry:javelin_steel 1",
            "spartanweaponry:boomerang_steel 1",
            "spartanweaponry:battleaxe_steel 1",
            "spartanweaponry:mace_steel 1",
            "spartanweaponry:glaive_steel 1",
            "spartanweaponry:staff_steel 1",
            "switchbow:switchcrossbow 1",
            "spartanweaponry:pike_iron 1",
            "spartanweaponry:lance_iron 1",
            "spartanweaponry:longbow_iron 1",
            "spartanweaponry:crossbow_iron 1",
            "spartanweaponry:throwing_knife_iron 1",
            "spartanweaponry:throwing_axe_iron 1",
            "spartanweaponry:javelin_iron 1",
            "spartanweaponry:boomerang_iron 1",
            "spartanweaponry:battleaxe_iron 1",
            "spartanweaponry:mace_iron 1",
            "spartanweaponry:glaive_iron 1",
            "spartanweaponry:staff_iron 1",
            "spartanweaponry:dagger_steel 1",
            "spartanweaponry:longsword_steel 1",
            "spartanweaponry:katana_steel 1",
            "spartanweaponry:scythe_steel 1",
            "spartanweaponry:saber_steel 1",
            "spartanweaponry:rapier_steel 1",
            "spartanweaponry:greatsword_steel 1",
            "spartanweaponry:hammer_steel 1",
            "spartanweaponry:warhammer_steel 1",
            "spartanweaponry:spear_steel 1",
            "spartanweaponry:halberd_steel 1",
            "spartanweaponry:pike_steel 1",
            "spartanweaponry:lance_steel 1",
            "spartanweaponry:longbow_steel 1",
            "spartanweaponry:crossbow_steel 1",
            "variedcommodities:hammer 1",
            "variedcommodities:lead_pipe 1",
            "variedcommodities:crowbar 1",
            "variedcommodities:pipe_wrench 1",
            "variedcommodities:wrench 1",
            "variedcommodities:candle 1",
            "variedcommodities:lamp 1",
            "variedcommodities:chain_skirt 1",
            "variedcommodities:iron_skirt 1",
            "spartanweaponry:mace_bronze 2",
            "spartanweaponry:glaive_bronze 2",
            "spartanweaponry:staff_bronze 2",
            "variedcommodities:bronze_sword 2",
            "mujmajnkraftsbettersurvival:itembronzehammer 2",
            "mujmajnkraftsbettersurvival:itembronzespear 2",
            "mujmajnkraftsbettersurvival:itembronzedagger 2",
            "mujmajnkraftsbettersurvival:itembronzebattleaxe 2",
            "mujmajnkraftsbettersurvival:itembronzenunchaku 2",
            "spartanshields:shield_basic_bronze 2",
            "spartanshields:shield_tower_bronze 2",
            "spartanweaponry:dagger_bronze 2",
            "spartanweaponry:longsword_bronze 2",
            "spartanweaponry:katana_bronze 2",
            "spartanweaponry:scythe_bronze 2",
            "spartanweaponry:saber_bronze 2",
            "spartanweaponry:rapier_bronze 2",
            "spartanweaponry:greatsword_bronze 2",
            "spartanweaponry:hammer_bronze 2",
            "spartanweaponry:warhammer_bronze 2",
            "spartanweaponry:spear_bronze 2",
            "spartanweaponry:halberd_bronze 2",
            "spartanweaponry:pike_bronze 2",
            "spartanweaponry:lance_bronze 2",
            "spartanweaponry:longbow_bronze 2",
            "spartanweaponry:crossbow_bronze 2",
            "spartanweaponry:throwing_knife_bronze 2",
            "spartanweaponry:throwing_axe_bronze 2",
            "spartanweaponry:javelin_bronze 2",
            "spartanweaponry:boomerang_bronze 2",
            "spartanweaponry:battleaxe_bronze 2",
            "spartanweaponry:longsword_gold 3",
            "spartanweaponry:katana_gold 3",
            "spartanweaponry:scythe_gold 3",
            "spartanweaponry:saber_gold 3",
            "spartanweaponry:rapier_gold 3",
            "spartanweaponry:greatsword_gold 3",
            "spartanweaponry:hammer_gold 3",
            "spartanweaponry:warhammer_gold 3",
            "spartanweaponry:spear_gold 3",
            "spartanweaponry:lance_gold 3",
            "minecraft:golden_sword 3",
            "minecraft:golden_shovel 3",
            "minecraft:golden_pickaxe 3",
            "minecraft:golden_axe 3",
            "minecraft:golden_hoe 3",
            "minecraft:golden_helmet 3",
            "minecraft:golden_chestplate 3",
            "minecraft:golden_leggings 3",
            "minecraft:golden_boots 3",
            "mujmajnkraftsbettersurvival:itemgoldhammer 3",
            "mujmajnkraftsbettersurvival:itemgoldspear 3",
            "mujmajnkraftsbettersurvival:itemgolddagger 3",
            "mujmajnkraftsbettersurvival:itemgoldbattleaxe 3",
            "mujmajnkraftsbettersurvival:itemgoldnunchaku 3",
            "bountifulbaubles:crowngold 3",
            "charm:gold_lantern 3",
            "fishingmadebetter:fish_tracker_gold 3",
            "locks:gold_lock_pick 3",
            "notreepunching:knife/gold 3",
            "notreepunching:mattock/gold 3",
            "notreepunching:saw/gold 3",
            "rustic:candle_gold 3",
            "rustic:candle_lever_gold 3",
            "rustic:golden_lantern 3",
            "spartanshields:shield_basic_gold 3",
            "spartanshields:shield_tower_gold 3",
            "spartanweaponry:dagger_gold 3",
            "spartanweaponry:halberd_gold 3",
            "spartanweaponry:pike_gold 3",
            "spartanweaponry:throwing_knife_gold 3",
            "spartanweaponry:throwing_axe_gold 3",
            "spartanweaponry:javelin_gold 3",
            "spartanweaponry:boomerang_gold 3",
            "spartanweaponry:battleaxe_gold 3",
            "spartanweaponry:mace_gold 3",
            "spartanweaponry:glaive_gold 3",
            "spartanweaponry:staff_gold 3",
            "variedcommodities:golden_skirt 3",
            "forgottenitems:hasty_pickaxe 3",
            "betternether:cincinnasite_pickaxe_diamond 3",
            "betternether:cincinnasite_pickaxe 3",
            "lycanitesmobs:sturdysummoningstaff 3",
            "variedcommodities:holyhandgrenade 3",
            "variedcommodities:pendant 3",
            "lycanitesmobs:stablesummoningstaff 3",
            "lycanitesmobs:savagesummoningstaff 3",
            "mod_lavacow:holy_grenade 3",
            "mod_lavacow:kings_crown 3",
            "minecraft:clock 3",
            "inspirations:redstone_charger 3",
            "trumpetskeleton:trumpet 3",
            "mod_lavacow:skeletonking_crown 3",
            "mod_lavacow:kings_crown 3 1",
            "lycanitesmobs:summoningstaff 3",
            "betternether:cincinnasite_axe 3",
            "locks:master_key 3",
            "lycanitesmobs:bloodsummoningstaff 3",
            "locks:key 3",
            "armorunder:liner_snips 3",
            "betternether:cincinnasite_axe_diamond 3",
            "spartanweaponry:pike_copper 4",
            "spartanweaponry:lance_copper 4",
            "spartanweaponry:longbow_copper 4",
            "spartanweaponry:crossbow_copper 4",
            "spartanweaponry:throwing_knife_copper 4",
            "spartanweaponry:throwing_axe_copper 4",
            "spartanweaponry:javelin_copper 4",
            "spartanweaponry:boomerang_copper 4",
            "spartanweaponry:battleaxe_copper 4",
            "mujmajnkraftsbettersurvival:itemcopperhammer 4",
            "mujmajnkraftsbettersurvival:itemcopperspear 4",
            "mujmajnkraftsbettersurvival:itemcopperdagger 4",
            "mujmajnkraftsbettersurvival:itemcopperbattleaxe 4",
            "mujmajnkraftsbettersurvival:itemcoppernunchaku 4",
            "iceandfire:armor_copper_metal_helmet 4",
            "iceandfire:armor_copper_metal_chestplate 4",
            "iceandfire:armor_copper_metal_leggings 4",
            "iceandfire:armor_copper_metal_boots 4",
            "iceandfire:copper_sword 4",
            "iceandfire:copper_shovel 4",
            "iceandfire:copper_pickaxe 4",
            "iceandfire:copper_axe 4",
            "iceandfire:copper_hoe 4",
            "spartanshields:shield_basic_copper 4",
            "spartanshields:shield_tower_copper 4",
            "spartanweaponry:dagger_copper 4",
            "spartanweaponry:longsword_copper 4",
            "spartanweaponry:scythe_copper 4",
            "spartanweaponry:saber_copper 4",
            "spartanweaponry:rapier_copper 4",
            "spartanweaponry:greatsword_copper 4",
            "spartanweaponry:hammer_copper 4",
            "spartanweaponry:warhammer_copper 4",
            "spartanweaponry:spear_copper 4",
            "spartanweaponry:halberd_copper 4",
            "spartanweaponry:katana_copper 4",
            "spartanweaponry:mace_copper 4",
            "spartanweaponry:glaive_copper 4",
            "spartanweaponry:staff_copper 4",
            "spartanweaponry:halberd_silver 5",
            "spartanweaponry:pike_silver 5",
            "spartanweaponry:lance_silver 5",
            "spartanweaponry:longbow_silver 5",
            "spartanweaponry:crossbow_silver 5",
            "spartanweaponry:throwing_knife_silver 5",
            "spartanweaponry:throwing_axe_silver 5",
            "spartanweaponry:javelin_silver 5",
            "spartanweaponry:boomerang_silver 5",
            "mujmajnkraftsbettersurvival:itemsilverhammer 5",
            "mujmajnkraftsbettersurvival:itemsilverspear 5",
            "mujmajnkraftsbettersurvival:itemsilverdagger 5",
            "mujmajnkraftsbettersurvival:itemsilverbattleaxe 5",
            "mujmajnkraftsbettersurvival:itemsilvernunchaku 5",
            "iceandfire:armor_silver_metal_helmet 5",
            "iceandfire:armor_silver_metal_chestplate 5",
            "iceandfire:armor_silver_metal_leggings 5",
            "iceandfire:armor_silver_metal_boots 5",
            "iceandfire:silver_sword 5",
            "iceandfire:silver_shovel 5",
            "iceandfire:silver_pickaxe 5",
            "iceandfire:silver_axe 5",
            "iceandfire:silver_hoe 5",
            "rustic:silver_lantern 5",
            "spartanshields:shield_basic_silver 5",
            "spartanshields:shield_tower_silver 5",
            "spartanweaponry:dagger_silver 5",
            "spartanweaponry:longsword_silver 5",
            "spartanweaponry:katana_silver 5",
            "spartanweaponry:scythe_silver 5",
            "spartanweaponry:saber_silver 5",
            "spartanweaponry:rapier_silver 5",
            "spartanweaponry:greatsword_silver 5",
            "spartanweaponry:hammer_silver 5",
            "spartanweaponry:warhammer_silver 5",
            "spartanweaponry:spear_silver 5",
            "spartanweaponry:battleaxe_silver 5",
            "spartanweaponry:mace_silver 5",
            "spartanweaponry:glaive_silver 5",
            "spartanweaponry:staff_silver 5",
            "rustic:candle_silver 5",
            "rustic:candle_lever_silver 5",
            "charm:charged_emerald 10",
            "aquaculture:fish 10 21",
            "advanced-fishing:fish 10 18",
            "spartanfire:katana_lightning_dragonbone 10",
            "spartanfire:scythe_lightning_dragonbone 10",
            "spartanfire:greatsword_lightning_dragonbone 10",
            "spartanfire:longsword_lightning_dragonbone 10",
            "spartanfire:saber_lightning_dragonbone 10",
            "spartanfire:rapier_lightning_dragonbone 10",
            "spartanfire:dagger_lightning_dragonbone 10",
            "spartanfire:spear_lightning_dragonbone 10",
            "spartanfire:pike_lightning_dragonbone 10",
            "spartanfire:lance_lightning_dragonbone 10",
            "spartanfire:halberd_lightning_dragonbone 10",
            "spartanfire:warhammer_lightning_dragonbone 10",
            "spartanfire:hammer_lightning_dragonbone 10",
            "spartanfire:throwing_axe_lightning_dragonbone 10",
            "spartanfire:throwing_knife_lightning_dragonbone 10",
            "spartanfire:longbow_lightning_dragonbone 10",
            "spartanfire:crossbow_lightning_dragonbone 10",
            "spartanfire:javelin_lightning_dragonbone 10",
            "spartanfire:battleaxe_lightning_dragonbone 10",
            "spartanfire:boomerang_lightning_dragonbone 10",
            "spartanfire:mace_lightning_dragonbone 10",
            "spartanfire:staff_lightning_dragonbone 10",
            "spartanfire:glaive_lightning_dragonbone 10",
            "mujmajnkraftsbettersurvival:itemlightningdragonbonehammer 10",
            "mujmajnkraftsbettersurvival:itemlightningdragonbonespear 10",
            "mujmajnkraftsbettersurvival:itemlightningdragonbonedagger 10",
            "mujmajnkraftsbettersurvival:itemlightningdragonbonebattleaxe 10",
            "mujmajnkraftsbettersurvival:itemlightningdragonbonenunchaku 10",
            "iceandfire:dragonbone_sword_lightning 10",
            "mujmajnkraftsbettersurvival:itemlumiumhammer 100"
    };

    //HashMap<Item, HashMap<Metadata, Conductivity>>
    private final Map<Item, Map<Integer,Integer>> conductivityMap = new HashMap<>();
    public int getItemConductivity(ItemStack stack) {
        if (conductivityMap.isEmpty()) {
            for (String entry : this.itemConductivities) {
                String[] split = entry.split(" ");
                if (split.length < 2) continue;
                Item item = Item.getByNameOrId(split[0].trim());
                if (item != null) {
                    try {
                        int conductivity = Integer.parseInt(split[1].trim());
                        int metadata = -1;
                        if(split.length>2)
                            metadata = Integer.parseInt(split[2].trim());
                        if(conductivityMap.containsKey(item))
                            conductivityMap.get(item).put(metadata,conductivity);
                        else {
                            Map<Integer,Integer> newEntry = new HashMap<>();
                            newEntry.put(metadata,conductivity);
                            conductivityMap.put(item, newEntry);
                        }
                    } catch (Exception exception) {
                        EagleMixins.LOGGER.error("Failed parsing item conductivity ({})", entry);
                    }
                }
            }
        }

        //Map contains the item
        if (conductivityMap.containsKey(stack.getItem())) {
            int metadata = stack.getMetadata();
            Map<Integer,Integer> conductivityByMetadata = conductivityMap.get(stack.getItem());
            if(conductivityByMetadata.containsKey(metadata))
                return conductivityByMetadata.get(metadata);
            else
                return conductivityByMetadata.getOrDefault(-1,0);
        }
        return 0;
    }
    public void reset(){
        conductivityMap.clear();
    }
}
