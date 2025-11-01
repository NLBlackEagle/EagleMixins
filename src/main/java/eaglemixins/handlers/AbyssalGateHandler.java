package eaglemixins.handlers;

import eaglemixins.util.Ref;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AbyssalGateHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClick (PlayerInteractEvent.RightClickBlock event) {

        EntityPlayer player = event.getEntityPlayer();
        World world = player.world;

        ItemStack heldItem = player.getHeldItemMainhand();
        if (heldItem.isEmpty()) return;

        Item quarkRune = Item.getByNameOrId("quark:rune");
        if (quarkRune == null || heldItem.getItem() != quarkRune) return;

        Block charmPortal = Block.getBlockFromName("charm:rune_portal_frame");
        if (charmPortal == null) return;

        Block clickedBlock = world.getBlockState(event.getPos()).getBlock();
        if (clickedBlock != charmPortal) return;

        if (Ref.entityIsInAbyssalGate(player)) {
            player.sendStatusMessage(new TextComponentTranslation("eaglemixins.message.abyssal_runes"), true);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player == null) return;
        if (event.getPos().getY() > 10) return;

        if (Ref.entityIsInAbyssalGate(player)) {
            player.sendStatusMessage(new TextComponentTranslation("eaglemixins.message.block_break_denied"), true);
            event.setCanceled(true);
        }
    }
}
