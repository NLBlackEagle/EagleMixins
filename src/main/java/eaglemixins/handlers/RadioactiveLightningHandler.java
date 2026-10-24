package eaglemixins.handlers;

import eaglemixins.capability.ChunkRadiationSource;
import eaglemixins.config.ForgeConfigHandler;
import nc.capability.radiation.source.IRadiationSource;
import nc.radiation.RadiationHelper;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Iterator;

public class RadioactiveLightningHandler {

    // Extra lightning where the radiation dust particles show, same chunk loop as vanilla's lightning in WorldServer.updateBlocks
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.world instanceof WorldServer)) return;
        if (!ForgeConfigHandler.lightning.radioactiveStorms) return;
        WorldServer world = (WorldServer) event.world;

        int chance = world.isThundering() ? ForgeConfigHandler.lightning.thunderChance
                : world.isRaining() ? ForgeConfigHandler.lightning.rainChance
                : ForgeConfigHandler.lightning.clearChance;
        if (chance <= 0) return;

        for (Iterator<Chunk> it = world.getPlayerChunkMap().getChunkIterator(); it.hasNext(); ) {
            Chunk chunk = it.next();
            if (world.rand.nextInt(chance) != 0 || !world.provider.canDoLightning(chunk)) continue;

            int x = (chunk.x << 4) + world.rand.nextInt(16);
            int z = (chunk.z << 4) + world.rand.nextInt(16);
            BlockPos pos = world.getPrecipitationHeight(new BlockPos(x, 0, z));
            if (!isHighRadiation(world, pos)) continue;

            world.addWeatherEffect(new EntityLightningBolt(world, pos.getX(), pos.getY(), pos.getZ(), false));
        }
    }

    /** Whether the radiation at pos is high enough to show the radiation dust particles */
    public static boolean isHighRadiation(World world, BlockPos pos) {
        IRadiationSource source = RadiationHelper.getRadiationSource(world.getChunk(pos));
        if (!(source instanceof ChunkRadiationSource)) return false;
        int subChunk = MathHelper.clamp(pos.getY() >> 4, 0, 15);
        return ((ChunkRadiationSource) source).getSubchunkRadiationLevel(subChunk) > ForgeConfigHandler.nuclear.rad_particle_threshold;
    }
}
