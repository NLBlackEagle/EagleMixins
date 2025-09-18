package eaglemixins.network;

import eaglemixins.client.gui.TeleportOverlayHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketStopTeleportOverlay implements IMessage {

    public PacketStopTeleportOverlay() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<PacketStopTeleportOverlay, IMessage> {
        @Override
        public IMessage onMessage(PacketStopTeleportOverlay message, MessageContext ctx) {
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(TeleportOverlayHandler::stop);
            return null;
        }
    }
}