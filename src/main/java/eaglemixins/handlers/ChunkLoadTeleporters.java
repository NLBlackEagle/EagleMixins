package eaglemixins.handlers;

import com.mojang.authlib.GameProfile;
import eaglemixins.EagleMixins;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class ChunkLoadTeleporters {

    private static final Logger LOGGER = LogManager.getLogger("EagleMixins");
    private static final String FAKE_PLAYER_NAME = "EagleMixins_Fake";

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();

        LOGGER.info("EagleMixins CHECK 1");

        // Skip client world or non-overworld
        if (!(world instanceof WorldServer) || world.isRemote || world.provider.getDimension() != 0) {
            return;
        }

        WorldServer worldServer = (WorldServer) world;

        LOGGER.info("EagleMixins CHECK 2");

        ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(EagleMixins.INSTANCE, worldServer, ForgeChunkManager.Type.NORMAL);
        if (ticket == null) {
            LOGGER.warn("EagleMixins Could not obtain chunk loading ticket for teleporters.");
            return;
        }

        LOGGER.info("EagleMixins Forcing teleport chunk loading...");

        int[][] blockCoords = {
                {-8247, -12929},
                {-14345, -6923},
                {-14394, 7793},
                {-2363, 15883},
                {13505, -8554},
                {3303, -13134},
                {7988, 13359}
        };

        int chunkRadius = 2;

        for (int[] coord : blockCoords) {
            int centerChunkX = coord[0] >> 4;
            int centerChunkZ = coord[1] >> 4;

            for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                    ChunkPos pos = new ChunkPos(centerChunkX + dx, centerChunkZ + dz);
                    ForgeChunkManager.forceChunk(ticket, pos);
                    LOGGER.info("EagleMixins Forced chunk at {}, {}", pos.x, pos.z);
                }
            }

            // After forcing chunks, simulate a player visit to trigger OTG structure population
            BlockPos teleportTarget = new BlockPos(coord[0], 64, coord[1]);
            simulateChunkVisit(worldServer, teleportTarget);
        }
    }

    /**
     * Teleports a fake player to the specified position to force OTG to populate the chunk.
     */
    private static void simulateChunkVisit(WorldServer world, BlockPos pos) {
        MinecraftServer server = world.getMinecraftServer();
        if (server == null) {
            LOGGER.warn("EagleMixins: No server found, skipping fake player teleport.");
            return;
        }

        GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes(FAKE_PLAYER_NAME.getBytes()), FAKE_PLAYER_NAME);
        FakePlayer fakePlayer = FakePlayerFactory.get(world, profile);

        fakePlayer.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        world.updateEntityWithOptionalForce(fakePlayer, true);

        LOGGER.info("EagleMixins: Teleported fake player to {}, {}, {}", pos.getX(), pos.getY(), pos.getZ());
    }
}