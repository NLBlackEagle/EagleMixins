package eaglemixins.init;

import eaglemixins.blocks.BlockDeepslate;
import eaglemixins.blocks.BlockDeepslateOre;
import eaglemixins.blocks.BlockDeepslateSlab;
import eaglemixins.blocks.BlockDeepslateStairs;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Deepslate and all its variants, ores, stairs and slabs (previously ContentTweaker/ContentCreator blocks in RLCraft Dregora).
 * Crafting recipes are json in assets/eaglemixins/recipes.
 */
@Mod.EventBusSubscriber
public final class DeepslateBlocks {

    // Blocks that get a plain ItemBlock
    private static final List<Block> SIMPLE_BLOCKS = new ArrayList<>();
    private static final List<BlockDeepslateSlab.HalfSlab> SLABS = new ArrayList<>();
    private static final List<BlockDeepslateSlab.DoubleSlab> DOUBLE_SLABS = new ArrayList<>();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e) {
        Block deepslate = register(e, new BlockDeepslate(3.0F), "deepslate");
        Block cobbled = register(e, new BlockDeepslate(3.5F), "cobbled_deepslate");
        Block polished = register(e, new BlockDeepslate(3.5F), "polished_deepslate");
        Block bricks = register(e, new BlockDeepslate(3.5F), "deepslate_bricks");
        Block tiles = register(e, new BlockDeepslate(3.5F), "deepslate_tiles");
        register(e, new BlockDeepslate(3.5F), "cracked_deepslate_bricks");
        register(e, new BlockDeepslate(3.5F), "cracked_deepslate_tiles");
        register(e, new BlockDeepslate(3.5F), "chiseled_deepslate");

        registerStairsAndSlab(e, deepslate, "deepslate_stairs", "deepslate_slab");
        registerStairsAndSlab(e, cobbled, "cobbled_deepslate_stairs", "cobbled_deepslate_slab");
        registerStairsAndSlab(e, polished, "polished_deepslate_stairs", "polished_deepslate_slab");
        registerStairsAndSlab(e, bricks, "deepslate_brick_stairs", "deepslate_brick_slab");
        registerStairsAndSlab(e, tiles, "deepslate_tile_stairs", "deepslate_tile_slab");

        // Ores with a drop source drop what that ore drops, the others drop themselves
        register(e, new BlockDeepslateOre(0, "minecraft:coal_ore"), "deepslate_coal_ore");
        register(e, new BlockDeepslateOre(1, null), "deepslate_iron_ore");
        register(e, new BlockDeepslateOre(2, null), "deepslate_gold_ore");
        register(e, new BlockDeepslateOre(2, "minecraft:redstone_ore"), "deepslate_redstone_ore");
        register(e, new BlockDeepslateOre(1, "minecraft:lapis_ore"), "deepslate_lapis_ore");
        register(e, new BlockDeepslateOre(2, "minecraft:emerald_ore"), "deepslate_emerald_ore");
        register(e, new BlockDeepslateOre(2, "minecraft:diamond_ore"), "deepslate_diamond_ore");
        register(e, new BlockDeepslateOre(2, null), "deepslate_silver_ore");
        register(e, new BlockDeepslateOre(1, null), "deepslate_copper_ore");
        register(e, new BlockDeepslateOre(1, null), "deepslate_lead_ore");
        register(e, new BlockDeepslateOre(2, null), "deepslate_thorium_ore");
        register(e, new BlockDeepslateOre(2, null), "deepslate_uranium_ore");
        register(e, new BlockDeepslateOre(2, null), "deepslate_boron_ore");
        register(e, new BlockDeepslateOre(2, null), "deepslate_lithium_ore");
        register(e, new BlockDeepslateOre(1, "scalinghealth:crystalore"), "deepslate_crystal_ore");
    }

    private static Block register(RegistryEvent.Register<Block> e, Block block, String name) {
        block.setRegistryName("eaglemixins", name).setTranslationKey("eaglemixins." + name);
        e.getRegistry().register(block);
        SIMPLE_BLOCKS.add(block);
        return block;
    }

    private static void registerStairsAndSlab(RegistryEvent.Register<Block> e, Block base, String stairsName, String slabName) {
        register(e, new BlockDeepslateStairs(base), stairsName);

        BlockDeepslateSlab.HalfSlab slab = new BlockDeepslateSlab.HalfSlab();
        slab.setRegistryName("eaglemixins", slabName).setTranslationKey("eaglemixins." + slabName);
        BlockDeepslateSlab.DoubleSlab doubleSlab = new BlockDeepslateSlab.DoubleSlab(slab);
        doubleSlab.setRegistryName("eaglemixins", slabName + "_double").setTranslationKey("eaglemixins." + slabName);
        e.getRegistry().registerAll(slab, doubleSlab);
        SLABS.add(slab);
        DOUBLE_SLABS.add(doubleSlab);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> e) {
        for (Block block : SIMPLE_BLOCKS) {
            e.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        }
        for (int i = 0; i < SLABS.size(); i++) {
            BlockDeepslateSlab.HalfSlab slab = SLABS.get(i);
            e.getRegistry().register(new ItemSlab(slab, slab, DOUBLE_SLABS.get(i)).setRegistryName(slab.getRegistryName()));
        }
    }

    // Namespaces of the old ContentTweaker/ContentCreator deepslate in RLCraft Dregora, same paths as here
    private static final List<String> LEGACY_NAMESPACES = Arrays.asList("contenttweaker", "contentcreator");

    // Converts deepslate blocks and items in old worlds once the ContentTweaker/ContentCreator scripts no longer register them
    @SubscribeEvent
    public static void remapLegacyBlocks(RegistryEvent.MissingMappings<Block> e) {
        for (RegistryEvent.MissingMappings.Mapping<Block> mapping : e.getAllMappings()) {
            ResourceLocation target = getLegacyTarget(mapping.key);
            if (target == null) continue;
            Block block = ForgeRegistries.BLOCKS.getValue(target);
            if (block != null && block != Blocks.AIR) mapping.remap(block);
        }
    }

    @SubscribeEvent
    public static void remapLegacyItems(RegistryEvent.MissingMappings<Item> e) {
        for (RegistryEvent.MissingMappings.Mapping<Item> mapping : e.getAllMappings()) {
            ResourceLocation target = getLegacyTarget(mapping.key);
            if (target == null) continue;
            Item item = ForgeRegistries.ITEMS.getValue(target);
            if (item != null && item != Items.AIR) mapping.remap(item);
        }
    }

    private static ResourceLocation getLegacyTarget(ResourceLocation key) {
        if (!LEGACY_NAMESPACES.contains(key.getNamespace()) || !key.getPath().contains("deepslate")) return null;
        return new ResourceLocation("eaglemixins", key.getPath());
    }

    /** Ore dictionary and smelting. Called from init, after all mods registered their items. */
    public static void init() {
        // Deepslate works like stone, cobbled deepslate like cobblestone (vanilla recipes use these ore dict entries)
        OreDictionary.registerOre("stone", ModBlocks.DEEPSLATE);
        OreDictionary.registerOre("cobblestone", ModBlocks.COBBLED_DEEPSLATE);

        oreDict("oreCoal", "deepslate_coal_ore");
        oreDict("oreIron", "deepslate_iron_ore");
        oreDict("oreGold", "deepslate_gold_ore");
        oreDict("oreRedstone", "deepslate_redstone_ore");
        oreDict("oreLapis", "deepslate_lapis_ore");
        oreDict("oreEmerald", "deepslate_emerald_ore");
        oreDict("oreDiamond", "deepslate_diamond_ore");
        oreDict("oreSilver", "deepslate_silver_ore");
        oreDict("oreCopper", "deepslate_copper_ore");
        oreDict("oreLead", "deepslate_lead_ore");
        oreDict("oreThorium", "deepslate_thorium_ore");
        oreDict("oreUranium", "deepslate_uranium_ore");
        oreDict("oreBoron", "deepslate_boron_ore");
        oreDict("oreLithium", "deepslate_lithium_ore");

        smelt("cobbled_deepslate", "eaglemixins:deepslate", 0, 0.0F);
        smelt("deepslate_bricks", "eaglemixins:cracked_deepslate_bricks", 0, 0.0F);
        smelt("deepslate_tiles", "eaglemixins:cracked_deepslate_tiles", 0, 0.0F);

        smelt("deepslate_coal_ore", "minecraft:coal", 0, 0.1F);
        smelt("deepslate_iron_ore", "minecraft:iron_ingot", 0, 0.1F);
        smelt("deepslate_gold_ore", "minecraft:gold_ingot", 0, 0.1F);
        smelt("deepslate_redstone_ore", "minecraft:redstone", 0, 0.1F);
        smelt("deepslate_lapis_ore", "minecraft:dye", 4, 0.1F);
        smelt("deepslate_emerald_ore", "minecraft:emerald", 0, 0.1F);
        smelt("deepslate_diamond_ore", "minecraft:diamond", 0, 0.1F);
        smelt("deepslate_silver_ore", "iceandfire:silver_ingot", 0, 0.1F);
        smelt("deepslate_copper_ore", "iceandfire:copper_ingot", 0, 0.1F);
        smelt("deepslate_lead_ore", "nuclearcraft:ingot", 2, 0.1F);
        smelt("deepslate_thorium_ore", "nuclearcraft:ingot", 3, 0.1F);
        smelt("deepslate_uranium_ore", "nuclearcraft:ingot", 4, 0.1F);
        smelt("deepslate_boron_ore", "nuclearcraft:ingot", 5, 0.1F);
        smelt("deepslate_lithium_ore", "nuclearcraft:ingot", 6, 0.1F);
        smelt("deepslate_crystal_ore", "scalinghealth:crystalshard", 0, 0.1F);
    }

    private static Block get(String name) {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation("eaglemixins", name));
    }

    private static void oreDict(String ore, String name) {
        OreDictionary.registerOre(ore, get(name));
    }

    private static void smelt(String input, String output, int meta, float xp) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(output));
        if (item != null && item != Items.AIR) GameRegistry.addSmelting(get(input), new ItemStack(item, 1, meta), xp);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e) {
        for (Block block : SIMPLE_BLOCKS) setItemModel(block);
        for (Block block : SLABS) setItemModel(block);
    }

    @SideOnly(Side.CLIENT)
    private static void setItemModel(Block block) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
    }

    private DeepslateBlocks() {}
}
