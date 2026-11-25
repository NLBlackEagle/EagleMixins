package eaglemixins.network;

import eaglemixins.client.AlphaWolfClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Marks a wolf as alpha on the client (for its skin), and whether it is stalking (for the stalking skin) */
public class PacketSyncAlphaWolf implements IMessage {

    private int entityId;
    private boolean stalking;

    public PacketSyncAlphaWolf() {
    }

    public PacketSyncAlphaWolf(Entity entity, boolean stalking) {
        this.entityId = entity.getEntityId();
        this.stalking = stalking;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        stalking = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(stalking);
    }

    public static class Handler implements IMessageHandler<PacketSyncAlphaWolf, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncAlphaWolf message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> AlphaWolfClientHandler.markAlpha(message.entityId, message.stalking));
            }
            return null;
        }
    }
}
