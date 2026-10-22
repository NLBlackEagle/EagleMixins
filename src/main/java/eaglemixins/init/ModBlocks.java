package eaglemixins.init;

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
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.oredict.OreDictionary;

/** Only block registry subscriber of the mod, so the registration order stays predictable. */
@Mod.EventBusSubscriber
@ObjectHolder("eaglemixins")
public final class ModBlocks {
    @ObjectHolder("deepslate")
    public static final Block DEEPSLATE = null; // injected after registration
    @ObjectHolder("cobbled_deepslate")
    public static final Block COBBLED_DEEPSLATE = null;
    @ObjectHolder("nether_thorium_ore")
    public static final Block NETHER_THORIUM_ORE = null;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e) {
        // Nether thorium ore only smelts into NuclearCraft thorium, so it only exists with NuclearCraft
        if (ModLoadedUtil.nuclearcraft.isLoaded()) e.getRegistry().register(
                new BlockNetherThoriumOre()
                        .setRegistryName("eaglemixins", "nether_thorium_ore")
                        .setTranslationKey("eaglemixins.nether_thorium_ore")
        );

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

    private ModBlocks() {}
}
