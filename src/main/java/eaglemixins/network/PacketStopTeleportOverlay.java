package eaglemixins.network;

import eaglemixins.client.gui.TeleportOverlayHandler;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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