package eaglemixins.handlers;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Mod.EventBusSubscriber
public class AbyssalGateHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClick (PlayerInteractEvent.RightClickBlock event) {

        EntityPlayer player = event.getEntityPlayer();
        World world = player.world;
        BlockPos pos = player.getPosition();

        ResourceLocation biomeName = world.getBiome(pos).getRegistryName();
        ItemStack heldItem = player.getHeldItemMainhand();
        Item quarkRune = ForgeRegistries.ITEMS.getValue(new ResourceLocation("quark", "rune"));
        Block charmPortal = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("charm", "rune_portal_frame"));
        Block clickedBlock = world.getBlockState(event.getPos()).getBlock();

        if (charmPortal == null) return;
        if (biomeName == null) return;
        if (heldItem.isEmpty()) return;
        if (quarkRune == null) return;
        if (clickedBlock != charmPortal) return;

        if (heldItem.getItem() == quarkRune && biomeName.toString().equals("openterraingenerator:overworld_abyssal_gate")) {
            player.sendStatusMessage(new TextComponentTranslation("eaglemixins.message.abyssal_runes"), true);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {

        EntityPlayer player = event.getPlayer();
        World world = event.getWorld();
        BlockPos pos = event.getPos();

        if (player == null) return;
        if (!(event.getPos().getY() <= 10)) return;

        ResourceLocation biomeName = world.getBiome(pos).getRegistryName();

        if (biomeName != null && biomeName.toString().equals("openterraingenerator:overworld_abyssal_gate")) {
            player.sendStatusMessage(new TextComponentTranslation("eaglemixins.message.block_break_denied"), true);
            event.setCanceled(true);
        }
    }
}
