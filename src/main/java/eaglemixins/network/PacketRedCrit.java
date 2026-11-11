package eaglemixins.network;

import eaglemixins.client.slam.ClientEffects;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Red crit burst on an entity, used when a Range Multiplier weapon (sentient lance) lands its distance bonus */
public class PacketRedCrit implements IMessage {

    private int entityId;

    public PacketRedCrit() {
    }

    public PacketRedCrit(Entity entity) {
        this.entityId = entity.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
    }

    public static class Handler implements IMessageHandler<PacketRedCrit, IMessage> {
        @Override
        public IMessage onMessage(PacketRedCrit message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> ClientEffects.redCrit(message.entityId));
            }
            return null;
        }
    }
}
