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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber
public final class ModItems {
    public static Item TAINTED_TEAR;
    // Replacements for the Brutal Coins in Asmodeus' summoning recipe (RLCraft Dregora), null if disabled
    public static Item VOID_TEAR;
    public static Item ENIGMOTH_EYE;
    public static Item GHOSTLY_GILLS;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> e) {
        if (ModLoadedUtil.nuclearcraft.isLoaded())
            e.getRegistry().register(new ItemBlock(ModBlocks.NETHER_THORIUM_ORE).setRegistryName(ModBlocks.NETHER_THORIUM_ORE.getRegistryName()));

        DeepslateBlocks.registerItems(e.getRegistry());

        TAINTED_TEAR = registerLoreItem(e, "tainted_tear");
        if (ForgeConfigHandler.server.asmodeusCraftingItems) {
            VOID_TEAR = registerLoreItem(e, "void_tear");
            ENIGMOTH_EYE = registerLoreItem(e, "enigmoth_eye");
            GHOSTLY_GILLS = registerLoreItem(e, "ghostly_gills");
        }
    }

    private static Item registerLoreItem(RegistryEvent.Register<Item> e, String name) {
        Item item = new ItemLore(name)
                .setRegistryName("eaglemixins", name)
                .setTranslationKey("eaglemixins." + name)
                .setCreativeTab(CreativeTabs.MATERIALS);
        e.getRegistry().register(item);
        return item;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e) {
        if (ModLoadedUtil.nuclearcraft.isLoaded()) setItemModel(Item.getItemFromBlock(ModBlocks.NETHER_THORIUM_ORE));
        DeepslateBlocks.registerModels();
        setItemModel(TAINTED_TEAR);
        if (ForgeConfigHandler.server.asmodeusCraftingItems) {
            setItemModel(VOID_TEAR);
            setItemModel(ENIGMOTH_EYE);
            setItemModel(GHOSTLY_GILLS);
        }
    }

    @SideOnly(Side.CLIENT)
    private static void setItemModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
