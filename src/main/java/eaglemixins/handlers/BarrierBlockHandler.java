package eaglemixins.handlers;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BarrierBlockHandler {

    private final static ResourceLocation BEDROCK = new ResourceLocation("dimstack:bedrock");
    private final static ResourceLocation PENDANT = new ResourceLocation("variedcommodities:pendant");
    private final static ResourceLocation ARTIFACT = new ResourceLocation("variedcommodities:artifact");

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if(event.getPlayer() != null && event.getPlayer().capabilities.isCreativeMode) return;
        IBlockState state = event.getState();
        Block block = state.getBlock();
        if (block.getRegistryName() == null || !block.getRegistryName().equals(BEDROCK)) {
            return;
        }
        int meta = block.getMetaFromState(state);
        if (meta == 0) {
            if (isIndestructible(event.getPlayer(), PENDANT)) {
                event.setCanceled(true);
            }
        } else if (meta == 5) {
            if (isIndestructible(event.getPlayer(), PENDANT)) {
                event.setCanceled(true);
            }
        } else if (meta == 6) {
            if (isIndestructible(event.getPlayer(), PENDANT)) {
                event.setCanceled(true);
            }
        } else if (meta == 7) {
            if (isIndestructible(event.getPlayer(), ARTIFACT)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if(event.getEntityPlayer().capabilities.isCreativeMode) return;
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        Block block = state.getBlock();
        if (block.getRegistryName() == null || !block.getRegistryName().equals(BEDROCK)) {
            return;
        }
        int meta = block.getMetaFromState(state);
        if (meta == 0) {
            if (isIndestructible(event.getEntityPlayer(), PENDANT)) {
                event.setCanceled(true);
            }
        } else if (meta == 5) {
            if (isIndestructible(event.getEntityPlayer(), PENDANT)) {
                event.setCanceled(true);
            }
        } else if (meta == 6) {
            if (isIndestructible(event.getEntityPlayer(), PENDANT)) {
                event.setCanceled(true);
            }
        } else if (meta == 7) {
            if (isIndestructible(event.getEntityPlayer(), ARTIFACT)) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean isIndestructible(EntityPlayer player, ResourceLocation requiredItem) {
        if (player == null) {
            return true;
        }
        if (itemMatches(player.getHeldItemMainhand(), requiredItem)) {
            return false;
        }
        return !itemMatches(player.getHeldItemOffhand(), requiredItem);
    }

    private static boolean itemMatches(ItemStack stack, ResourceLocation match) {
        return !stack.isEmpty()
                && stack.getItem().getRegistryName() != null
                && stack.getItem().getRegistryName().equals(match);
    }
}
