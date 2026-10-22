package eaglemixins.init;

import eaglemixins.blocks.BlockLockSlave;
import eaglemixins.blocks.BlockLocked;
import eaglemixins.blocks.TileEntityLock;
import eaglemixins.items.ItemLockKey;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Lock block (keyblock.bbmodel), keyslave (keyslave.bbmodel) and the one-time key.
 * Keys and locks are paired through the LockId NBT tag, see LockedBlockConfig.
 */
@Mod.EventBusSubscriber(modid = "eaglemixins")
public final class LockedBlocks {

    private static BlockLocked lock;
    private static Block keyslave;
    private static Item key;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e) {
        lock = new BlockLocked();
        lock.setRegistryName("eaglemixins", "locked_block").setTranslationKey("eaglemixins.locked_block");
        keyslave = new BlockLockSlave().setRegistryName("eaglemixins", "keyslave").setTranslationKey("eaglemixins.keyslave");
        e.getRegistry().registerAll(lock, keyslave);
        GameRegistry.registerTileEntity(TileEntityLock.class, new ResourceLocation("eaglemixins", "locked_block"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> e) {
        key = new ItemLockKey().setRegistryName("eaglemixins", "lock_key").setTranslationKey("eaglemixins.lock_key");
        e.getRegistry().registerAll(
                new ItemLockKey.ItemBlockLocked(lock).setRegistryName(lock.getRegistryName()),
                new ItemBlock(keyslave).setRegistryName(keyslave.getRegistryName()),
                key
        );
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(lock), 0, new ModelResourceLocation(lock.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(keyslave), 0, new ModelResourceLocation(keyslave.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(key, 0, new ModelResourceLocation(key.getRegistryName(), "inventory"));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerItemColors(ColorHandlerEvent.Item e) {
        e.getItemColors().registerItemColorHandler((stack, tintIndex) -> tintIndex == 0 ? ItemLockKey.getColor(ItemLockKey.getLockId(stack)) : -1, key);
    }

    private LockedBlocks() {}
}
