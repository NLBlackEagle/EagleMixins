package eaglemixins.network;

import eaglemixins.client.slam.ClientEffects;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Ground slam (earthquake wave) at a position, sent when the Sentient Maul's kill ability triggers */
public class PacketMaulSlam implements IMessage {

    private double x, y, z;
    private float radius;

    public PacketMaulSlam() {
    }

    public PacketMaulSlam(double x, double y, double z, float radius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        radius = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(radius);
    }

    public static class Handler implements IMessageHandler<PacketMaulSlam, IMessage> {
        @Override
        public IMessage onMessage(PacketMaulSlam message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> ClientEffects.groundSlam(message.x, message.y, message.z, message.radius));
            }
            return null;
        }
    }
}
