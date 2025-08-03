package eaglemixins.handlers;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class BlockNoclipHandler {

    private static final List<String> MATCH_PATTERNS = Arrays.asList(
            "comforts:sleeping_bag", "comforts:hammock",
            "variedcommodities:chair", "variedcommodities:couch_wool",
            "variedcommodities:couch_wood", "variedcommodities:stool",
            "minecraft:bed"
    );

    private static final Predicate<String> matchWildcard = registryName ->
            MATCH_PATTERNS.stream().anyMatch(registryName::startsWith);

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onRightClickBlock(RightClickBlock event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        Block block = world.getBlockState(pos).getBlock();

        ResourceLocation rl = Block.REGISTRY.getNameForObject(block);
        if (rl == null) return;

        String fullName = rl.toString(); // e.g., comforts:sleeping_bag_white

        if (!matchWildcard.test(fullName)) return;

        BlockPos posAbove = pos.up();
        Material matAbove = world.getBlockState(posAbove).getMaterial();

        // Cancel if block above is not air or a liquid
        if (!matAbove.isReplaceable() && !matAbove.isLiquid() && matAbove != Material.AIR) {
            event.setCanceled(true);
        }
    }
}