package eaglemixins.handlers;

import eaglemixins.client.TeleportOverlayHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TeleportDimensionHandler {
    private static int lastDim = Integer.MIN_VALUE;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) return;

        int currentDim = mc.player.dimension;
        if (currentDim != lastDim) {
            if (currentDim == 3 ) {
                TeleportOverlayHandler.trigger(false);
            }
            lastDim = currentDim;
        }
    }
}
