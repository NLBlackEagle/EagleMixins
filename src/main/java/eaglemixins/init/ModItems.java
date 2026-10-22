package eaglemixins.init;

import eaglemixins.compat.ModLoadedUtil;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.items.ItemLore;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Only item registry subscriber of the mod, so the registration order stays predictable. */
@Mod.EventBusSubscriber
@ObjectHolder("eaglemixins")
public final class ModItems {
    @ObjectHolder("tainted_tear")
    public static final Item TAINTED_TEAR = null; // injected after registration
    @ObjectHolder("void_tear")
    public static final Item VOID_TEAR = null;
    @ObjectHolder("enigmoth_eye")
    public static final Item ENIGMOTH_EYE = null;
    @ObjectHolder("ghostly_gills")
    public static final Item GHOSTLY_GILLS = null;

    // Replacements for the Brutal Coins in Asmodeus' summoning recipe (RLCraft Dregora)
    private static final String[] ASMODEUS_CRAFTING_ITEMS = {"void_tear", "enigmoth_eye", "ghostly_gills"};

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> e) {
        if (ModLoadedUtil.nuclearcraft.isLoaded())
            e.getRegistry().register(new ItemBlock(ModBlocks.NETHER_THORIUM_ORE).setRegistryName(ModBlocks.NETHER_THORIUM_ORE.getRegistryName()));

        DeepslateBlocks.registerItems(e.getRegistry());

        e.getRegistry().register(
                new ItemLore("tainted_tear")
                        .setRegistryName("eaglemixins", "tainted_tear")
                        .setTranslationKey("eaglemixins.tainted_tear")
                        .setCreativeTab(CreativeTabs.MATERIALS)
        );

        if (ForgeConfigHandler.server.asmodeusCraftingItems) for (String name : ASMODEUS_CRAFTING_ITEMS) {
            e.getRegistry().register(
                    new ItemLore(name)
                            .setRegistryName("eaglemixins", name)
                            .setTranslationKey("eaglemixins." + name)
                            .setCreativeTab(CreativeTabs.MATERIALS)
            );
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e) {
        if (ModLoadedUtil.nuclearcraft.isLoaded()) ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(ModBlocks.NETHER_THORIUM_ORE), 0,
                new ModelResourceLocation("eaglemixins:nether_thorium_ore", "inventory")
        );
        DeepslateBlocks.registerModels();
        ModelLoader.setCustomModelResourceLocation(
                TAINTED_TEAR, 0,
                new ModelResourceLocation("eaglemixins:tainted_tear", "inventory")
        );
        if (ForgeConfigHandler.server.asmodeusCraftingItems) for (String name : ASMODEUS_CRAFTING_ITEMS) {
            ModelLoader.setCustomModelResourceLocation(
                    Item.getByNameOrId("eaglemixins:" + name), 0,
                    new ModelResourceLocation("eaglemixins:" + name, "inventory")
            );
        }
    }

    private ModItems() {}
}
