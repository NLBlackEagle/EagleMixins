package eaglemixins.handlers;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NuclearCraftInteractions {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        BlockPos pos = event.getPos();
        World world = event.getWorld();
        Block block = world.getBlockState(pos).getBlock();

        if (block.getRegistryName() != null && (block.getRegistryName().getNamespace().equals("nuclearcraft"))) {
            // If Reskillable tried to block the interaction, override it
            if (event.isCanceled()) {
                event.setCanceled(false);
            }

            // Force interaction to proceed
            event.setUseBlock(net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW);
            event.setUseItem(net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW);
        }
    }
}