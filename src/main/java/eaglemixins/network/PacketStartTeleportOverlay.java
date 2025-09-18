package eaglemixins.network;

import eaglemixins.client.gui.TeleportOverlayHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketStartTeleportOverlay implements IMessage {
    private boolean isGlitch;

    // Required no-arg constructor
    public PacketStartTeleportOverlay() {}

    public PacketStartTeleportOverlay(boolean isGlitch) {
        this.isGlitch = isGlitch;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(isGlitch);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        isGlitch = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<PacketStartTeleportOverlay, IMessage> {
        @Override
        public IMessage onMessage(PacketStartTeleportOverlay message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() ->
                    TeleportOverlayHandler.trigger(message.isGlitch)
            );
            return null;
        }
    }
}
