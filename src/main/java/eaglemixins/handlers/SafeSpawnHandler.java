package eaglemixins.handlers;

import eaglemixins.EagleMixins;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.SafeSpawnConfig;
import eaglemixins.handlers.util.SafeSpawnPlayer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import javax.annotation.Nullable;

/**
 * Random spawn points are picked when a player is created, often in chunks that only have terrain yet:
 * OTG places its objects (BO3s) when a chunk gets populated, which can bury the player afterwards.
 * Once a player actually stays at its random spawn point (first join, respawn without a bed), the area around it
 * is fully generated and the player is moved to a safe spot, preferably inside a building.
 */
public class SafeSpawnHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        relocate(event.player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        relocate(event.player);
    }

    private static void relocate(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP) || !(player instanceof SafeSpawnPlayer) || !(player.world instanceof WorldServer)) return;
        BlockPos randomSpawn = ((SafeSpawnPlayer) player).eaglemixins$getRandomSpawn();
        // Players with a saved position or a bed were moved away from it
        if (randomSpawn == null || MathHelper.floor(player.posX) != randomSpawn.getX() || MathHelper.floor(player.posZ) != randomSpawn.getZ()) return;

        BlockPos spot = findSpot((WorldServer) player.world, randomSpawn);
        if (spot == null) return;
        player.fallDistance = 0;
        ((EntityPlayerMP) player).connection.setPlayerLocation(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, player.rotationYaw, player.rotationPitch);
        EagleMixins.LOGGER.debug("Moved random spawn of {} from {} to {}", player.getName(), randomSpawn, spot);
    }

    /** Feet position of the nearest spot inside a building, or else the nearest highest safe spot. Null if there is none. */
    @Nullable
    public static BlockPos findSpot(WorldServer world, BlockPos origin) {
        SafeSpawnConfig config = ForgeConfigHandler.safespawn;
        int radius = config.searchRadius;

        // A chunk gets its objects when it and its neighbours are populated, which needs one more chunk of generated terrain around those
        int minX = (origin.getX() - radius) >> 4, maxX = (origin.getX() + radius) >> 4;
        int minZ = (origin.getZ() - radius) >> 4, maxZ = (origin.getZ() + radius) >> 4;
        for (int cx = minX - 1; cx <= maxX + 1; cx++)
            for (int cz = minZ - 1; cz <= maxZ + 1; cz++)
                world.getChunk(cx, cz);

        // Stay in the biome of the random spawn point, which already passed respawn biome blacklists
        Biome biome = world.getBiome(origin);
        BlockPos surface = null, interior = null;
        int surfaceDist = Integer.MAX_VALUE, interiorDist = Integer.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int dist = dx * dx + dz * dz;
                if (dist > radius * radius || (dist >= surfaceDist && dist >= interiorDist)) continue;
                BlockPos column = origin.add(dx, 0, dz);
                if (world.getBiome(column) != biome) continue;

                Column result = scan(world, column, config);
                if (result.surface != null && dist < surfaceDist) {
                    surface = result.surface;
                    surfaceDist = dist;
                }
                if (result.interior != null && dist < interiorDist) {
                    interior = result.interior;
                    interiorDist = dist;
                }
            }
        }
        return config.preferStructures && interior != null ? interior : surface;
    }

    private static class Column {
        @Nullable BlockPos surface; // highest safe spot with open sky above it (ground or a rooftop)
        @Nullable BlockPos interior; // highest safe spot under a built roof
    }

    /**
     * Goes down the column. The first solid block (not leaves or logs) is the roof: if it is built, the first safe spot
     * below it is inside a building. Below natural terrain there are only caves, so the scan stops there.
     */
    private static Column scan(WorldServer world, BlockPos column, SafeSpawnConfig config) {
        Column result = new Column();
        Chunk chunk = world.getChunk(column);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int roofY = -1;
        for (int y = chunk.getTopFilledSegment() + 15; y > 1; y--) {
            pos.setPos(column.getX(), y, column.getZ());
            if (isSafeSpot(world, pos)) {
                if (roofY < 0) {
                    if (result.surface == null) result.surface = pos.toImmutable();
                } else if (roofY - y <= config.maxRoofHeight) {
                    result.interior = pos.toImmutable();
                    break;
                } else {
                    break;
                }
            }
            if (roofY < 0 && isRoof(world, pos)) {
                if (!isBuilt(world, pos, config)) break;
                roofY = y;
            }
        }
        return result;
    }

    /** Feet and head fit, standing on something solid that doesn't hurt */
    private static boolean isSafeSpot(WorldServer world, BlockPos.MutableBlockPos feet) {
        if (!isPassable(world.getBlockState(feet)) || !isPassable(world.getBlockState(feet.up()))) return false;
        IBlockState floor = world.getBlockState(feet.down());
        return floor.getMaterial().blocksMovement() && !floor.getMaterial().isLiquid()
                && !floor.getBlock().isLeaves(floor, world, feet.down())
                && floor.getBlock() != Blocks.CACTUS && floor.getBlock() != Blocks.MAGMA;
    }

    private static boolean isPassable(IBlockState state) {
        Material material = state.getMaterial();
        return !material.blocksMovement() && !material.isLiquid() && material != Material.FIRE && material != Material.CACTUS;
    }

    private static boolean isRoof(WorldServer world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getMaterial().blocksMovement() && !state.getMaterial().isLiquid()
                && !state.getBlock().isLeaves(state, world, pos) && !state.getBlock().isWood(world, pos);
    }

    private static boolean isBuilt(WorldServer world, BlockPos pos, SafeSpawnConfig config) {
        IBlockState state = world.getBlockState(pos);
        Material material = state.getMaterial();
        if (material == Material.GROUND || material == Material.GRASS || material == Material.SAND || material == Material.CLAY
                || material == Material.SNOW || material == Material.CRAFTED_SNOW || material == Material.ICE || material == Material.PACKED_ICE
                || material == Material.GOURD || material == Material.PLANTS || material == Material.VINE) return false;
        return !config.isNatural(state.getBlock());
    }
}
