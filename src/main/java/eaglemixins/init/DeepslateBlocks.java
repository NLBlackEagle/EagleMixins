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
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Deepslate and all its variants, ores, stairs and slabs (previously ContentTweaker/ContentCreator blocks in RLCraft Dregora).
 * Registration is called from the registry events in ModBlocks and ModItems, only the legacy remapping subscribes here.
 * Crafting recipes are json in assets/eaglemixins/recipes.
 */
@Mod.EventBusSubscriber
public final class DeepslateBlocks {

    // Blocks that get a plain ItemBlock
    private static final List<Block> SIMPLE_BLOCKS = new ArrayList<>();
    private static final List<BlockDeepslateSlab.HalfSlab> SLABS = new ArrayList<>();
    private static final List<BlockDeepslateSlab.DoubleSlab> DOUBLE_SLABS = new ArrayList<>();

    public static void registerBlocks(IForgeRegistry<Block> registry) {
        ModBlocks.DEEPSLATE = register(registry, new BlockDeepslate(3.0F), "deepslate");
        ModBlocks.COBBLED_DEEPSLATE = register(registry, new BlockDeepslate(3.5F), "cobbled_deepslate");
        ModBlocks.POLISHED_DEEPSLATE = register(registry, new BlockDeepslate(3.5F), "polished_deepslate");
        ModBlocks.DEEPSLATE_BRICKS = register(registry, new BlockDeepslate(3.5F), "deepslate_bricks");
        ModBlocks.DEEPSLATE_TILES = register(registry, new BlockDeepslate(3.5F), "deepslate_tiles");
        ModBlocks.CRACKED_DEEPSLATE_BRICKS = register(registry, new BlockDeepslate(3.5F), "cracked_deepslate_bricks");
        ModBlocks.CRACKED_DEEPSLATE_TILES = register(registry, new BlockDeepslate(3.5F), "cracked_deepslate_tiles");
        ModBlocks.CHISELED_DEEPSLATE = register(registry, new BlockDeepslate(3.5F), "chiseled_deepslate");

        ModBlocks.DEEPSLATE_STAIRS = register(registry, new BlockDeepslateStairs(ModBlocks.DEEPSLATE), "deepslate_stairs");
        ModBlocks.COBBLED_DEEPSLATE_STAIRS = register(registry, new BlockDeepslateStairs(ModBlocks.COBBLED_DEEPSLATE), "cobbled_deepslate_stairs");
        ModBlocks.POLISHED_DEEPSLATE_STAIRS = register(registry, new BlockDeepslateStairs(ModBlocks.POLISHED_DEEPSLATE), "polished_deepslate_stairs");
        ModBlocks.DEEPSLATE_BRICK_STAIRS = register(registry, new BlockDeepslateStairs(ModBlocks.DEEPSLATE_BRICKS), "deepslate_brick_stairs");
        ModBlocks.DEEPSLATE_TILE_STAIRS = register(registry, new BlockDeepslateStairs(ModBlocks.DEEPSLATE_TILES), "deepslate_tile_stairs");

        ModBlocks.DEEPSLATE_SLAB = registerSlab(registry, "deepslate_slab");
        ModBlocks.COBBLED_DEEPSLATE_SLAB = registerSlab(registry, "cobbled_deepslate_slab");
        ModBlocks.POLISHED_DEEPSLATE_SLAB = registerSlab(registry, "polished_deepslate_slab");
        ModBlocks.DEEPSLATE_BRICK_SLAB = registerSlab(registry, "deepslate_brick_slab");
        ModBlocks.DEEPSLATE_TILE_SLAB = registerSlab(registry, "deepslate_tile_slab");
        ModBlocks.DEEPSLATE_SLAB_DOUBLE = registerDoubleSlab(registry, ModBlocks.DEEPSLATE_SLAB);
        ModBlocks.COBBLED_DEEPSLATE_SLAB_DOUBLE = registerDoubleSlab(registry, ModBlocks.COBBLED_DEEPSLATE_SLAB);
        ModBlocks.POLISHED_DEEPSLATE_SLAB_DOUBLE = registerDoubleSlab(registry, ModBlocks.POLISHED_DEEPSLATE_SLAB);
        ModBlocks.DEEPSLATE_BRICK_SLAB_DOUBLE = registerDoubleSlab(registry, ModBlocks.DEEPSLATE_BRICK_SLAB);
        ModBlocks.DEEPSLATE_TILE_SLAB_DOUBLE = registerDoubleSlab(registry, ModBlocks.DEEPSLATE_TILE_SLAB);

        // Ores with a drop source drop what that ore drops, the others drop themselves
        ModBlocks.DEEPSLATE_COAL_ORE = register(registry, new BlockDeepslateOre(0, "minecraft:coal_ore"), "deepslate_coal_ore");
        ModBlocks.DEEPSLATE_IRON_ORE = register(registry, new BlockDeepslateOre(1, null), "deepslate_iron_ore");
        ModBlocks.DEEPSLATE_GOLD_ORE = register(registry, new BlockDeepslateOre(2, null), "deepslate_gold_ore");
        ModBlocks.DEEPSLATE_REDSTONE_ORE = register(registry, new BlockDeepslateOre(2, "minecraft:redstone_ore"), "deepslate_redstone_ore");
        ModBlocks.DEEPSLATE_LAPIS_ORE = register(registry, new BlockDeepslateOre(1, "minecraft:lapis_ore"), "deepslate_lapis_ore");
        ModBlocks.DEEPSLATE_EMERALD_ORE = register(registry, new BlockDeepslateOre(2, "minecraft:emerald_ore"), "deepslate_emerald_ore");
        ModBlocks.DEEPSLATE_DIAMOND_ORE = register(registry, new BlockDeepslateOre(2, "minecraft:diamond_ore"), "deepslate_diamond_ore");
        ModBlocks.DEEPSLATE_SILVER_ORE = register(registry, new BlockDeepslateOre(2, null), "deepslate_silver_ore");
        ModBlocks.DEEPSLATE_COPPER_ORE = register(registry, new BlockDeepslateOre(1, null), "deepslate_copper_ore");
        ModBlocks.DEEPSLATE_LEAD_ORE = register(registry, new BlockDeepslateOre(1, null), "deepslate_lead_ore");
        ModBlocks.DEEPSLATE_THORIUM_ORE = register(registry, new BlockDeepslateOre(2, null), "deepslate_thorium_ore");
        ModBlocks.DEEPSLATE_URANIUM_ORE = register(registry, new BlockDeepslateOre(2, null), "deepslate_uranium_ore");
        ModBlocks.DEEPSLATE_BORON_ORE = register(registry, new BlockDeepslateOre(2, null), "deepslate_boron_ore");
        ModBlocks.DEEPSLATE_LITHIUM_ORE = register(registry, new BlockDeepslateOre(2, null), "deepslate_lithium_ore");
        ModBlocks.DEEPSLATE_CRYSTAL_ORE = register(registry, new BlockDeepslateOre(1, "scalinghealth:crystalore"), "deepslate_crystal_ore");
    }

    private static Block register(IForgeRegistry<Block> registry, Block block, String name) {
        block.setRegistryName("eaglemixins", name).setTranslationKey("eaglemixins." + name);
        registry.register(block);
        SIMPLE_BLOCKS.add(block);
        return block;
    }

    private static BlockDeepslateSlab.HalfSlab registerSlab(IForgeRegistry<Block> registry, String name) {
        BlockDeepslateSlab.HalfSlab slab = new BlockDeepslateSlab.HalfSlab();
        slab.setRegistryName("eaglemixins", name).setTranslationKey("eaglemixins." + name);
        registry.register(slab);
        SLABS.add(slab);
        return slab;
    }

    // Double slabs share the translation key of their half slab
    private static BlockDeepslateSlab.DoubleSlab registerDoubleSlab(IForgeRegistry<Block> registry, BlockDeepslateSlab.HalfSlab half) {
        String name = half.getRegistryName().getPath();
        BlockDeepslateSlab.DoubleSlab doubleSlab = new BlockDeepslateSlab.DoubleSlab(half);
        doubleSlab.setRegistryName("eaglemixins", name + "_double").setTranslationKey("eaglemixins." + name);
        registry.register(doubleSlab);
        DOUBLE_SLABS.add(doubleSlab);
        return doubleSlab;
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        for (Block block : SIMPLE_BLOCKS) {
            registry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        }
        for (int i = 0; i < SLABS.size(); i++) {
            BlockDeepslateSlab.HalfSlab slab = SLABS.get(i);
            registry.register(new ItemSlab(slab, slab, DOUBLE_SLABS.get(i)).setRegistryName(slab.getRegistryName()));
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

        OreDictionary.registerOre("oreCoal", ModBlocks.DEEPSLATE_COAL_ORE);
        OreDictionary.registerOre("oreIron", ModBlocks.DEEPSLATE_IRON_ORE);
        OreDictionary.registerOre("oreGold", ModBlocks.DEEPSLATE_GOLD_ORE);
        OreDictionary.registerOre("oreRedstone", ModBlocks.DEEPSLATE_REDSTONE_ORE);
        OreDictionary.registerOre("oreLapis", ModBlocks.DEEPSLATE_LAPIS_ORE);
        OreDictionary.registerOre("oreEmerald", ModBlocks.DEEPSLATE_EMERALD_ORE);
        OreDictionary.registerOre("oreDiamond", ModBlocks.DEEPSLATE_DIAMOND_ORE);
        OreDictionary.registerOre("oreSilver", ModBlocks.DEEPSLATE_SILVER_ORE);
        OreDictionary.registerOre("oreCopper", ModBlocks.DEEPSLATE_COPPER_ORE);
        OreDictionary.registerOre("oreLead", ModBlocks.DEEPSLATE_LEAD_ORE);
        OreDictionary.registerOre("oreThorium", ModBlocks.DEEPSLATE_THORIUM_ORE);
        OreDictionary.registerOre("oreUranium", ModBlocks.DEEPSLATE_URANIUM_ORE);
        OreDictionary.registerOre("oreBoron", ModBlocks.DEEPSLATE_BORON_ORE);
        OreDictionary.registerOre("oreLithium", ModBlocks.DEEPSLATE_LITHIUM_ORE);

        GameRegistry.addSmelting(ModBlocks.COBBLED_DEEPSLATE, new ItemStack(ModBlocks.DEEPSLATE), 0.0F);
        GameRegistry.addSmelting(ModBlocks.DEEPSLATE_BRICKS, new ItemStack(ModBlocks.CRACKED_DEEPSLATE_BRICKS), 0.0F);
        GameRegistry.addSmelting(ModBlocks.DEEPSLATE_TILES, new ItemStack(ModBlocks.CRACKED_DEEPSLATE_TILES), 0.0F);

        smelt(ModBlocks.DEEPSLATE_COAL_ORE, "minecraft:coal", 0, 0.1F);
        smelt(ModBlocks.DEEPSLATE_IRON_ORE, "minecraft:iron_ingot", 0, 0.1F);
        smelt(ModBlocks.DEEPSLATE_GOLD_ORE, "minecraft:gold_ingot", 0, 0.1F);
        smelt(ModBlocks.DEEPSLATE_REDSTONE_ORE, "minecraft:redstone", 0, 0.1F);
        smelt(ModBlocks.DEEPSLATE_LAPIS_ORE, "minecraft:dye", 4, 0.1F);
        smelt(ModBlocks.DEEPSLATE_EMERALD_ORE, "minecraft:emerald", 0, 0.1F);
        smelt(ModBlocks.DEEPSLATE_DIAMOND_ORE, "minecraft:diamond", 0, 0.1F);
        smelt(ModBlocks.DEEPSLATE_SILVER_ORE, "iceandfire:silver_ingot", 0, 0.1F);
        smelt(ModBlocks.DEEPSLATE_COPPER_ORE, "iceandfire:copper_ingot", 0, 0.1F);
        smelt(ModBlocks.DEEPSLATE_LEAD_ORE, "nuclearcraft:ingot", 2, 0.1F);
        smelt(ModBlocks.DEEPSLATE_THORIUM_ORE, "nuclearcraft:ingot", 3, 0.1F);
        smelt(ModBlocks.DEEPSLATE_URANIUM_ORE, "nuclearcraft:ingot", 4, 0.1F);
        smelt(ModBlocks.DEEPSLATE_BORON_ORE, "nuclearcraft:ingot", 5, 0.1F);
        smelt(ModBlocks.DEEPSLATE_LITHIUM_ORE, "nuclearcraft:ingot", 6, 0.1F);
        smelt(ModBlocks.DEEPSLATE_CRYSTAL_ORE, "scalinghealth:crystalshard", 0, 0.1F);
    }

    // Output from another mod, skipped if that mod isn't loaded
    private static void smelt(Block input, String output, int meta, float xp) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(output));
        if (item != null && item != Items.AIR) GameRegistry.addSmelting(input, new ItemStack(item, 1, meta), xp);
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        for (Block block : SIMPLE_BLOCKS) setItemModel(block);
        for (Block block : SLABS) setItemModel(block);
    }

    @SideOnly(Side.CLIENT)
    private static void setItemModel(Block block) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
    }

    private DeepslateBlocks() {}
}
