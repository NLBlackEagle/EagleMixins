package eaglemixins.handlers;

import com.Fishmod.mod_LavaCow.entities.EntitySkeletonKing;
import com.Fishmod.mod_LavaCow.init.FishItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SkeletonKingSpawnHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {

        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) return;

        ItemStack usedStack = event.getItemStack();
        Item usedItem = usedStack.getItem();
        if (usedItem != FishItems.KINGS_CROWN || usedStack.getMetadata() != 1) return;

        //TODO: otg get biome instead of using this clientside function
        String biomeName = event.getWorld().getBiome(player.getPosition()).getBiomeName();
        if (!biomeName.contains("Desert") && !biomeName.contains("Dune") && !biomeName.contains("Wasteland")) return;

        switch (event.getHand()) {
            case MAIN_HAND: player.getHeldItemMainhand().shrink(1);
            case OFF_HAND: player.getHeldItemOffhand().shrink(1);
        }

        RayTraceResult spawnPosRay = player.rayTrace(10, 1);
        if (spawnPosRay == null) return;
        BlockPos spawnPos = spawnPosRay.getBlockPos();

        EntitySkeletonKing skeletonKing = new EntitySkeletonKing(event.getWorld());
        skeletonKing.setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        event.getWorld().spawnEntity(skeletonKing);
    }
}
