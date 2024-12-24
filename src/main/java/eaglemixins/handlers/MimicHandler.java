package eaglemixins.handlers;


import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


import java.util.Random;

public class MimicHandler {



    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerContainerEvent(PlayerContainerEvent event, Container container)

    {

        //if container contents are not generated yet
        BlockPos posBlock = ((BlockPos) event.getEntityPlayer().getPositionVector());

        if (container.getInventory() == null) {


            //if container position is lower than y25

            //if random is under 5% create mimic
        }
    }
}
