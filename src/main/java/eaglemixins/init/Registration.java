package eaglemixins.init;

import eaglemixins.blocks.BlockDeepslate;
import eaglemixins.items.ItemTaintedTear;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = "eaglemixins")
public final class Registration {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e) {
        e.getRegistry().register(
                new BlockDeepslate()
                        .setRegistryName("eaglemixins", "deepslate")
                        .setTranslationKey("eaglemixins.deepslate")
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> e) {
        Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("eaglemixins","deepslate"));
        if (b == null) throw new IllegalStateException("Block not registered: eaglemixins:deepslate");

        ItemBlock ib = new ItemBlock(b);
        ib.setRegistryName(b.getRegistryName()); // MUST exactly match the block’s registry name
        e.getRegistry().register(ib);

        e.getRegistry().register(
                new ItemTaintedTear()
                        .setRegistryName("eaglemixins", "tainted_tear")
                        .setTranslationKey("eaglemixins.tainted_tear")
                        .setCreativeTab(CreativeTabs.MATERIALS)
        );
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e) {
        ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(ModBlocks.DEEPSLATE), 0,
                new ModelResourceLocation("eaglemixins:deepslate", "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                ModItems.TAINTED_TEAR, 0,
                new ModelResourceLocation("eaglemixins:tainted_tear", "inventory")
        );
    }
}

