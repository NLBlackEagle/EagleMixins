package eaglemixins.handlers;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class FallDamageNegation {

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        //max fall dmg from changing dimension to underneath is dmg from 10 blocks falling
        if (event.toDim == 3) event.player.fallDistance = Math.min(event.player.fallDistance, 10);
    }
}
