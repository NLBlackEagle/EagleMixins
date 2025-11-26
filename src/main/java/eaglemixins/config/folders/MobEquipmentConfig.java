package eaglemixins.config.folders;

import com.google.common.collect.ArrayListMultimap;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.common.config.Config;

import java.util.*;

public class MobEquipmentConfig {
    @Config.Comment("Pattern: I:\"modid:itemid\"=weight")
    @Config.Name("Zombie Hand Items")
    public Map<String, Integer> zombieHand = new HashMap<String, Integer>(){{
        put("minecraft:iron_sword", 1);
        put("minecraft:iron_shovel", 2);
    }};

    @Config.Comment("Pattern: I:\"modid:itemid\"=weight")
    @Config.Name("Skeleton Hand Items")
    public Map<String, Integer> skeletonHand = new HashMap<String, Integer>(){{
        put("minecraft:bow", 1);
    }};

    @Config.Comment("Applied at least to zombies and skeletons but also to various other mobs extending from them\n" +
            "Pattern: mod_id, helmet_id, chestplate_id, leggings_id, boot_id, tier\n" +
            "Leave slots empty if the armor set doesn't have a piece for that slot. Example: somemod, , onlychest, , , 1, 1\n" +
            "Default: 0: Leather Tier, 1: Gold Tier, 2: Chainmail Tier, 3: Iron Tier, 4: Diamond Tier, >4 custom to be defined if max tier is increased")
    @Config.Name("Armor Sets")
    public String[] armor = {
            "minecraft, leather_helmet, leather_chestplate, leather_leggings, leather_boots, 0, 1",
            "minecraft, golden_helmet, golden_chestplate, golden_leggings, golden_boots, 1, 1",
            "minecraft, chainmail_helmet, chainmail_chestplate, chainmail_leggings, chainmail_boots, 2, 1",
            "minecraft, iron_helmet, iron_chestplate, iron_leggings, iron_boots, 3, 1",
            "minecraft, diamond_helmet, diamond_chestplate, diamond_leggings, diamond_boots, 4, 1"
    };


    @Config.Comment("Allows Strays and Wither Skeletons to use offhand vanilla tipped arrows and allows any skeleton to use offhand arrow items.")
    @Config.Name("Enable Offhand Arrows For All Skeletons")
    public boolean enabledModdedArrowsForAll = true;

    @Config.Comment("Base chance multiplier for zombie types getting weapons. By default 5% in hard mode, 1% in all other difficulties. The given multiplier here will be multiplied on top of those.")
    @Config.Name("Zombie Weapon Base Chance Multi")
    @Config.RangeDouble(min = Float.MIN_VALUE, max = 100)
    public float baseZombieChanceMulti = 1F;

    @Config.Comment("Base chance for mobs getting armor. This is multiplied by the local difficulty ratio (0 to 1), so the given value is only reached once a chunk is inhabited for a long time. Default: 15%")
    @Config.Name("Armor Base Chance")
    @Config.RangeDouble(min = 0)
    public float baseArmorChance = 0.15F;

    @Config.Comment("Starts with a 50/50 chance either at tier 0 or tier 1, then continues to roll with this given chance to increase tier until max tier. Default: roll with 9.5% chance for each additional tier increase")
    @Config.Name("Armor Tier Increase Chance")
    @Config.RangeDouble(min = 0, max = 1)
    public float armorTierIncreaseChance = 0.095F;

    @Config.Comment("Max tier of mob equipment armor. Each tier is exponentially less likely (see \"Armor Tier Increase Chance\"). If you increase this value you want to provide at least one armor set for each additional tier.")
    @Config.Name("Armor Max Tier")
    @Config.RangeInt(min = 1)
    public int armorMaxTier = 4;

    @Config.Comment("This system is a bit hard to comprehend. Once vanilla decided to give a mob at least one armor piece, for each possible additional armor piece on that mob it will roll with a chance of either 90% (hard mode) or 75% (any other difficulty) to add more armor pieces. \n" +
            "The given multiplier here will be multiplied on the 90% or 75%, so a number bigger than 1 will increase the chance for mobs having more than one armor piece, while a number below 1 will reduce it.")
    @Config.Name("Additional Armor Piece Chance Multi")
    @Config.RangeDouble(min = 0, max = 1.34F)
    public float additionalArmorChanceMulti = 1.0F;

    @Config.Comment("Settings for enchants on the mob equipment")
    @Config.Name("Enchants")
    public EnchantConfig enchants = new EnchantConfig();

    @Config.Comment("Settings for skeletons using Spartan Crossbows and Longbows")
    @Config.Name("Skeletons Using SpartanWeaponry")
    public SpartanWeaponrySkeletonsConfig spartanSkeletons = new SpartanWeaponrySkeletonsConfig();

    public static class EnchantConfig {
        @Config.Comment("Minimum enchantability enchanted mainhand items will have")
        @Config.Name("Mainhand - Min Ench")
        @Config.RangeInt(min = 1)
        public int mainhand_minEnch = 5;

        @Config.Comment("Maximum enchantability enchanted mainhand items will have. This is only reached if the local clamped difficulty is 1, so if the area is inhabited for a while.")
        @Config.Name("Mainhand - Max Ench")
        @Config.RangeInt(min = 1)
        public int mainhand_maxEnch = 22;

        @Config.Comment("Allow treasure enchantments on enchanted mainhand items?")
        @Config.Name("Mainhand - Allow Treasure Enchants")
        public boolean mainhand_allowTreasure = false;

        @Config.Comment("Chance for mainhand items to be enchanted. Scaled with local clamped difficulty, so the given value will only be reached if the area is inhabited for a while.")
        @Config.Name("Mainhand - Chance")
        public float mainhand_chanceEnchant = 0.25F;

        @Config.Comment("Minimum enchantability enchanted armor pieces will have")
        @Config.Name("Armor - Min Ench")
        @Config.RangeInt(min = 1)
        public int armor_minEnch = 5;

        @Config.Comment("Maximum enchantability enchanted armor pieces will have. This is only reached if the local clamped difficulty is 1, so if the area is inhabited for a while.")
        @Config.Name("Armor - Max Ench")
        @Config.RangeInt(min = 1)
        public int armor_maxEnch = 22;

        @Config.Comment("Allow treasure enchantments on enchanted armor pieces?")
        @Config.Name("Armor - Allow Treasure Enchants")
        public boolean armor_allowTreasure = false;

        @Config.Comment("Chance for armor pieces to be enchanted. Scaled with local clamped difficulty, so the given value will only be reached if the area is inhabited for a while.")
        @Config.Name("Armor - Chance")
        public float armor_chanceEnchant = 0.5F;
    }


    public static class SpartanWeaponrySkeletonsConfig{

        @Config.Comment("The range/projectile speed stat will provide a strafing penalty based on the difference between the vanilla bow.")
        @Config.Name("Enable Move Speed Penalty")
        public boolean enableMoveSpeedPenalty = true;

        @Config.Comment("The range/projectile speed stat will provide an op1 Follow Range Attribute bonus based on the difference between the vanilla bow.")
        @Config.Name("Enable Follow Range Bonus")
        public boolean enableFollowRangeBonus = true;

        @Config.Comment("The range/projectile speed stat will provide a strafing distance bonus based on the difference between the vanilla bow.")
        @Config.Name("Enable AI Strafe Distance Bonus")
        public boolean enableStrafeDistanceBonus = true;
    }

    public static final List<ItemEntry> zombieHands = new ArrayList<>();
    public static final List<ItemEntry> skeletonHands = new ArrayList<>();
    public static final ArrayListMultimap<Integer, ItemSetEntry> armorByTier = ArrayListMultimap.create();

    public void reset(){
        zombieHands.clear();
        skeletonHands.clear();
        armorByTier.clear();
    }

    public static Item getRandomItem(Random rand, boolean forZombie) {
        if (forZombie && zombieHands.isEmpty())
            ForgeConfigHandler.mobequipment.zombieHand.forEach((itemid, weight) -> zombieHands.add(new ItemEntry(Item.getByNameOrId(itemid), weight)));
        else if (!forZombie && skeletonHands.isEmpty())
            ForgeConfigHandler.mobequipment.skeletonHand.forEach((itemid, weight) -> skeletonHands.add(new ItemEntry(Item.getByNameOrId(itemid), weight)));

        if (forZombie) return WeightedRandom.getRandomItem(rand, zombieHands).item;
        else return WeightedRandom.getRandomItem(rand, skeletonHands).item;
    }

    public static ItemSetEntry getRandomArmor(Random rand, int tier) {
        if (armorByTier.isEmpty()) {
            for (String s : ForgeConfigHandler.mobequipment.armor) {
                String[] split = s.split(",");
                String modid = split[0].trim();
                String helmet = split[1].trim();
                String chest = split[2].trim();
                String legs = split[3].trim();
                String boots = split[4].trim();
                int parsedTier = Integer.parseInt(split[5].trim());
                int weight = Integer.parseInt(split[6].trim());

                armorByTier.put(parsedTier, new ItemSetEntry(modid, helmet, chest, legs, boots, weight));
            }
        }

        return WeightedRandom.getRandomItem(rand, armorByTier.get(tier));
    }

    public static class ItemEntry extends WeightedRandom.Item {
        public final Item item;
        public ItemEntry(Item item, int weight){
            super(weight);
            this.item = item == null ? Items.AIR : item;
        }
    }

    public static class ItemSetEntry extends WeightedRandom.Item {
        public final Item helmet, chest, legs, boots;
        public ItemSetEntry(String modid, String helmet, String chest, String legs, String boots, int weight){
            super(weight);

            Item tmp = Item.getByNameOrId(modid + ":" + helmet);
            this.helmet = tmp == null ? Items.AIR : tmp;

            tmp = Item.getByNameOrId(modid + ":" + chest);
            this.chest = tmp == null ? Items.AIR : tmp;

            tmp = Item.getByNameOrId(modid + ":" + legs);
            this.legs = tmp == null ? Items.AIR : tmp;

            tmp = Item.getByNameOrId(modid + ":" + boots);
            this.boots = tmp == null ? Items.AIR : tmp;
        }

        public Item getItemForSlot(EntityEquipmentSlot slotIn) {
            switch (slotIn){
                case HEAD: return helmet;
                case CHEST: return chest;
                case LEGS: return legs;
                case FEET: return boots;
                default: return Items.AIR;
            }
        }
    }
}
