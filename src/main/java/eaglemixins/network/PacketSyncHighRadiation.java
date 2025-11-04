package eaglemixins.network;

import eaglemixins.capability.ChunkRadiationSource;
import eaglemixins.config.ForgeConfigHandler;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ShortMap;
import it.unimi.dsi.fastutil.longs.Long2ShortOpenHashMap;
import nc.capability.radiation.source.IRadiationSource;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSyncHighRadiation implements IMessage {

    private final Long2ShortMap data = new Long2ShortOpenHashMap();

    public PacketSyncHighRadiation() {

    }

    public PacketSyncHighRadiation(EntityPlayer player) {
        int chunkX = MathHelper.floor(player.posX) >> 4;
        int chunkZ = MathHelper.floor(player.posZ) >> 4;
        int r = 5;
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                if (x * x + z * z > r * r) {
                    continue;
                }
                Chunk chunk = player.world.getChunkProvider().getLoadedChunk(chunkX + x, chunkZ + z);
                if (chunk == null) {
                    continue;
                }
                IRadiationSource radiation = chunk.getCapability(IRadiationSource.CAPABILITY_RADIATION_SOURCE, null);
                if (!(radiation instanceof ChunkRadiationSource)) {
                    continue;
                }
                short highRadiation = 0;
                for (int y = 0; y < 16; y++) {
                    if (((ChunkRadiationSource) radiation).getSubchunkRadiationLevel(y) > ForgeConfigHandler.nuclear.rad_particle_threshold) {
                        highRadiation |= 1 << y;
                    }
                }
                this.data.put(ChunkPos.asLong(chunkX + x, chunkZ + z), highRadiation);
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        for (int i = buf.readByte(); i > 0; i--) {
            this.data.put(buf.readLong(), buf.readShort());
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.data.size());
        for (Long2ShortMap.Entry e : this.data.long2ShortEntrySet()) {
            buf.writeLong(e.getLongKey());
            buf.writeShort(e.getShortValue());
        }
    }

    public static class Handler implements IMessageHandler<PacketSyncHighRadiation, IMessage> {

        @Override
        public IMessage onMessage(PacketSyncHighRadiation message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> PacketProcessor.onMessage(message));
            }
            return null;
        }

        private static class PacketProcessor {

            public static void onMessage(PacketSyncHighRadiation message) {
                World world = Minecraft.getMinecraft().world;
                for (Long2ShortMap.Entry e : message.data.long2ShortEntrySet()) {
                    Chunk chunk = world.getChunk((int) e.getLongKey(), (int) (e.getLongKey() >> 32));
                    IRadiationSource radiation = chunk.getCapability(IRadiationSource.CAPABILITY_RADIATION_SOURCE, null);
                    if (!(radiation instanceof ChunkRadiationSource)) {
                        return;
                    }
                    ((ChunkRadiationSource) radiation).setHighRadiation(e.getShortValue());
                }
            }

        }

    }

}
