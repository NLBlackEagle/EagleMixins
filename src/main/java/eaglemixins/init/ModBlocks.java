package eaglemixins.init;

import eaglemixins.blocks.BlockDeepslateSlab;
import eaglemixins.blocks.BlockNetherThoriumOre;
import eaglemixins.compat.ModLoadedUtil;
import eaglemixins.world.NetherThoriumGenerator;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

@Mod.EventBusSubscriber
public final class ModBlocks {
    // Only registered with NuclearCraft, null otherwise
    public static Block NETHER_THORIUM_ORE;

    // Deepslate, registered in DeepslateBlocks
    public static Block DEEPSLATE;
    public static Block COBBLED_DEEPSLATE;
    public static Block POLISHED_DEEPSLATE;
    public static Block DEEPSLATE_BRICKS;
    public static Block DEEPSLATE_TILES;
    public static Block CRACKED_DEEPSLATE_BRICKS;
    public static Block CRACKED_DEEPSLATE_TILES;
    public static Block CHISELED_DEEPSLATE;

    public static Block DEEPSLATE_STAIRS;
    public static Block COBBLED_DEEPSLATE_STAIRS;
    public static Block POLISHED_DEEPSLATE_STAIRS;
    public static Block DEEPSLATE_BRICK_STAIRS;
    public static Block DEEPSLATE_TILE_STAIRS;

    public static BlockDeepslateSlab.HalfSlab DEEPSLATE_SLAB;
    public static BlockDeepslateSlab.HalfSlab COBBLED_DEEPSLATE_SLAB;
    public static BlockDeepslateSlab.HalfSlab POLISHED_DEEPSLATE_SLAB;
    public static BlockDeepslateSlab.HalfSlab DEEPSLATE_BRICK_SLAB;
    public static BlockDeepslateSlab.HalfSlab DEEPSLATE_TILE_SLAB;
    public static BlockDeepslateSlab.DoubleSlab DEEPSLATE_SLAB_DOUBLE;
    public static BlockDeepslateSlab.DoubleSlab COBBLED_DEEPSLATE_SLAB_DOUBLE;
    public static BlockDeepslateSlab.DoubleSlab POLISHED_DEEPSLATE_SLAB_DOUBLE;
    public static BlockDeepslateSlab.DoubleSlab DEEPSLATE_BRICK_SLAB_DOUBLE;
    public static BlockDeepslateSlab.DoubleSlab DEEPSLATE_TILE_SLAB_DOUBLE;

    public static Block DEEPSLATE_COAL_ORE;
    public static Block DEEPSLATE_IRON_ORE;
    public static Block DEEPSLATE_GOLD_ORE;
    public static Block DEEPSLATE_REDSTONE_ORE;
    public static Block DEEPSLATE_LAPIS_ORE;
    public static Block DEEPSLATE_EMERALD_ORE;
    public static Block DEEPSLATE_DIAMOND_ORE;
    public static Block DEEPSLATE_SILVER_ORE;
    public static Block DEEPSLATE_COPPER_ORE;
    public static Block DEEPSLATE_LEAD_ORE;
    public static Block DEEPSLATE_THORIUM_ORE;
    public static Block DEEPSLATE_URANIUM_ORE;
    public static Block DEEPSLATE_BORON_ORE;
    public static Block DEEPSLATE_LITHIUM_ORE;
    public static Block DEEPSLATE_CRYSTAL_ORE;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e) {
        // Nether thorium ore only smelts into NuclearCraft thorium, so it only exists with NuclearCraft
        if (ModLoadedUtil.nuclearcraft.isLoaded()) {
            NETHER_THORIUM_ORE = new BlockNetherThoriumOre()
                    .setRegistryName("eaglemixins", "nether_thorium_ore")
                    .setTranslationKey("eaglemixins.nether_thorium_ore");
            e.getRegistry().register(NETHER_THORIUM_ORE);
        }

        DeepslateBlocks.registerBlocks(e.getRegistry());
    }

    /** Ore dictionary, smelting and world gen. Called from init, after all mods registered their items. */
    public static void init() {
        DeepslateBlocks.init();

        if (ModLoadedUtil.nuclearcraft.isLoaded()) {
            OreDictionary.registerOre("oreThorium", NETHER_THORIUM_ORE);
            Item ncIngot = ForgeRegistries.ITEMS.getValue(new ResourceLocation("nuclearcraft", "ingot"));
            if (ncIngot != null) GameRegistry.addSmelting(NETHER_THORIUM_ORE, new ItemStack(ncIngot, 1, 3), 0.1F); // meta 3 = thorium

            GameRegistry.registerWorldGenerator(new NetherThoriumGenerator(), 0);
        }
    }
}
