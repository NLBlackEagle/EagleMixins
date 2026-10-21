package eaglemixins.handlers;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.biomes.ForgeBiome;
import com.pg85.otg.forge.commands.BaseCommand;
import com.pg85.otg.forge.dimensions.OTGTeleporter;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.generator.biome.BiomeGenerator;
import com.pg85.otg.generator.biome.OutputType;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.OTGBiomeSearchConfig;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Backs the /otg tp biome search (see OTGTPCommandMixin).
 * <p>
 * OTG generates the biome map for the whole search square in one call, which freezes the server and leaves
 * huge arrays in OTG's static ArraysCache pool forever. This searches outwards from the player in rings of small tiles
 * instead, stops as soon as the nearest match is certain and spreads the work over server ticks with a time budget.
 * OTG's biome layers are not thread safe, so this has to stay on the server thread.
 */
public class OTGBiomeSearchHandler {

    /** Tile edge length in biome-generation cells (1 cell = 4x4 blocks), so 512 blocks per tile. */
    private static final int TILE = 128;

    private static final Map<UUID, Search> SEARCHES = new LinkedHashMap<>();

    public static boolean onTpCommand(BaseCommand command, ICommandSender sender, List<String> args) {
        if (args.isEmpty()) return false;

        EntityPlayerMP player;
        int playerFlag = args.indexOf("-p");
        if (playerFlag >= 0) {
            String name = args.get(args.size() - 1);
            player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(name);
            if (player == null) {
                sender.sendMessage(error("Could not find player " + name));
                return true;
            }
        } else if (sender.getCommandSenderEntity() instanceof EntityPlayerMP) {
            player = (EntityPlayerMP) sender.getCommandSenderEntity();
        } else {
            sender.sendMessage(error("Must be a player to send this command without player argument tp"));
            return true;
        }

        List<String> nameArgs = new ArrayList<>(playerFlag >= 0 ? args.subList(0, playerFlag) : args);
        if (nameArgs.isEmpty()) return false;

        OTGBiomeSearchConfig config = ForgeConfigHandler.otgbiomesearch;
        int radius = config.defaultRadius;
        // A trailing number is the radius, unless it is the only argument (then it's a biome id)
        if (nameArgs.size() >= 2) {
            Integer parsed = tryParseInt(nameArgs.get(nameArgs.size() - 1));
            if (parsed != null) {
                radius = parsed;
                nameArgs.remove(nameArgs.size() - 1);
            }
        }
        String targetName = String.join(" ", nameArgs).trim();
        if (targetName.isEmpty()) return false;

        if (radius < 1) {
            sender.sendMessage(error("Radius must be positive"));
            return true;
        }
        if (radius > config.maxRadius) {
            sender.sendMessage(message("Radius " + radius + " exceeds the maximum, using " + config.maxRadius + " instead."));
            radius = config.maxRadius;
        }

        sender.sendMessage(message("Searching for destination biome or dimension \"" + BaseCommand.VALUE_COLOR + targetName + BaseCommand.MESSAGE_COLOR + "\"."));

        for (int i = -1; i < 1024; ++i) {
            if (!DimensionManager.isDimensionRegistered(i)) continue;
            DimensionType dimensionType = DimensionManager.getProviderType(i);
            if (dimensionType.getName().toLowerCase().trim().equals(targetName.toLowerCase())) {
                sender.sendMessage(message("Dimension found, teleport commenced"));
                OTGTeleporter.changeDimension(i, player, false, true);
                return true;
            }
        }

        ForgeWorld world = ((ForgeEngine) OTG.getEngine()).getWorld(player.world);
        if (world == null) {
            sender.sendMessage(error("Player is not in an OTG world"));
            return true;
        }

        Integer biomeId = tryParseInt(targetName.replace(" ", ""));
        LocalBiome target = biomeId == null ? world.getBiomeByNameOrNull(targetName) : world.getBiomeByOTGIdOrNull(biomeId);
        if (target == null) {
            sender.sendMessage(error("Could not find biome \"" + targetName + "\"."));
            return true;
        }

        BiomeGenerator generator = world.getBiomeGenerator();
        if (generator == null || !generator.canGenerateUnZoomed()) {
            sender.sendMessage(error("This world's biome mode does not support searching for biomes."));
            return true;
        }

        Search previous = SEARCHES.remove(player.getUniqueID());
        if (previous != null) previous.tell(message("Previous biome search for \"" + previous.targetName + "\" was replaced."));

        SEARCHES.put(player.getUniqueID(), new Search(sender, player, world, generator.unwrap(), (ForgeBiome) target, targetName, radius));
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || SEARCHES.isEmpty()) return;

        long deadline = System.nanoTime() + ForgeConfigHandler.otgbiomesearch.tickBudgetMs * 1_000_000L;
        // Round-robin one tile per search until the budget is used up
        while (!SEARCHES.isEmpty() && System.nanoTime() < deadline) {
            Iterator<Search> it = SEARCHES.values().iterator();
            while (it.hasNext() && System.nanoTime() < deadline) {
                if (it.next().step()) it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        SEARCHES.values().removeIf(search -> search.world.getWorld() == event.getWorld());
    }

    private static Integer tryParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static TextComponentString message(String text) {
        return new TextComponentString(BaseCommand.MESSAGE_COLOR + text);
    }

    private static TextComponentString error(String text) {
        return new TextComponentString(BaseCommand.ERROR_COLOR + text);
    }

    private static class Search {
        private final ICommandSender sender;
        /** The player object is replaced on respawn, so only keep its id and look it up every step */
        private final UUID playerId;
        private final String playerName;
        private final boolean senderIsPlayer;
        private final ForgeWorld world;
        private final BiomeGenerator generator;
        private final int targetId;
        private final String targetName;

        private final int radius;
        private final long radiusSq;
        private final int originX, originZ;
        /** Cell (biome-gen coordinate) the player stands in, tiles are centered around it */
        private final int originCellX, originCellZ;

        private final int[] buffer = new int[TILE * TILE];
        private int ring = 0;
        private int ringIndex = 0;

        private boolean found = false;
        private int bestX, bestZ;
        private long bestDistSq = Long.MAX_VALUE;

        private final long startNanos = System.nanoTime();
        private long lastProgressNanos = startNanos;

        Search(ICommandSender sender, EntityPlayerMP player, ForgeWorld world, BiomeGenerator generator, ForgeBiome target, String targetName, int radius) {
            this.sender = sender;
            this.playerId = player.getUniqueID();
            this.playerName = player.getName();
            this.senderIsPlayer = sender == player;
            this.world = world;
            this.generator = generator;
            this.targetId = target.getIds().getOTGBiomeId();
            this.targetName = targetName;
            this.radius = radius;
            this.radiusSq = (long) radius * radius;
            this.originX = (int) Math.floor(player.posX);
            this.originZ = (int) Math.floor(player.posZ);
            this.originCellX = originX >> 2;
            this.originCellZ = originZ >> 2;
        }

        /** Processes one tile. Returns true when the search is done (found, exhausted or cancelled). */
        boolean step() {
            EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(playerId);
            if (player == null || player.world != world.getWorld()) {
                if (!senderIsPlayer) sender.sendMessage(error("Biome search for \"" + targetName + "\" cancelled, " + playerName + " left the dimension."));
                else if (player != null) player.sendMessage(error("Biome search for \"" + targetName + "\" cancelled, you left the dimension."));
                return true;
            }

            // Every cell in this ring is at least this far away, so if nothing closer can exist we're done
            long ringMinDist = Math.max(0L, ((long) ring * TILE - TILE / 2 - 1) * 4L);
            if (ringMinDist * ringMinDist >= Math.min(radiusSq + 1, bestDistSq)) {
                finish(player);
                return true;
            }

            int tileCount = ring == 0 ? 1 : 8 * ring;
            scanTile(ring, ringIndex);
            if (++ringIndex >= tileCount) {
                ring++;
                ringIndex = 0;
            }

            sendProgress();
            return false;
        }

        private void scanTile(int r, int index) {
            int tx, tz;
            if (r == 0) {
                tx = 0;
                tz = 0;
            } else {
                // Walk the ring's perimeter: top, right, bottom, left side, each 2r tiles long
                int side = index / (2 * r);
                int offset = index % (2 * r);
                switch (side) {
                    case 0: tx = -r + offset; tz = -r; break;
                    case 1: tx = r; tz = -r + offset; break;
                    case 2: tx = r - offset; tz = r; break;
                    default: tx = -r; tz = r - offset; break;
                }
            }

            int cellX0 = originCellX + tx * TILE - TILE / 2;
            int cellZ0 = originCellZ + tz * TILE - TILE / 2;

            // Skip tiles that can't contain anything inside the radius or closer than the current best
            long nearestDx = clamp(originX, cellX0 * 4 + 2, (cellX0 + TILE - 1) * 4 + 2) - originX;
            long nearestDz = clamp(originZ, cellZ0 * 4 + 2, (cellZ0 + TILE - 1) * 4 + 2) - originZ;
            long tileMinDistSq = nearestDx * nearestDx + nearestDz * nearestDz;
            if (tileMinDistSq > radiusSq || tileMinDistSq >= bestDistSq) return;

            int[] ids = generator.getBiomesUnZoomed(buffer, cellX0, cellZ0, TILE, TILE, OutputType.DEFAULT_FOR_WORLD);
            for (int i = 0; i < TILE * TILE; i++) {
                if (ids[i] != targetId) continue;
                int x = (cellX0 + i % TILE) * 4 + 2;
                int z = (cellZ0 + i / TILE) * 4 + 2;
                long dx = x - originX;
                long dz = z - originZ;
                long distSq = dx * dx + dz * dz;
                if (distSq <= radiusSq && distSq < bestDistSq) {
                    found = true;
                    bestDistSq = distSq;
                    bestX = x;
                    bestZ = z;
                }
            }
        }

        private void tell(TextComponentString text) {
            if (!senderIsPlayer) {
                sender.sendMessage(text);
                return;
            }
            EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(playerId);
            if (player != null) player.sendMessage(text);
        }

        private void finish(EntityPlayerMP player) {
            String seconds = String.format("%.1f", (System.nanoTime() - startNanos) / 1.0E9);
            if (!found) {
                tell(error("Could not find biome \"" + targetName + "\" within " + radius + " blocks (searched for " + seconds + "s)."));
                return;
            }

            World mcWorld = world.getWorld();
            BlockPos destination = mcWorld.getTopSolidOrLiquidBlock(new BlockPos(bestX, 0, bestZ));
            tell(message("Teleporting to \"" + BaseCommand.VALUE_COLOR + targetName + BaseCommand.MESSAGE_COLOR + "\" at "
                    + bestX + ", " + bestZ + " (" + (int) Math.sqrt(bestDistSq) + " blocks away, searched for " + seconds + "s)."));
            player.setPositionAndUpdate(bestX + 0.5, destination.getY(), bestZ + 0.5);
        }

        private void sendProgress() {
            int interval = ForgeConfigHandler.otgbiomesearch.progressIntervalSeconds;
            if (interval <= 0) return;
            long now = System.nanoTime();
            if (now - lastProgressNanos < interval * 1_000_000_000L) return;
            lastProgressNanos = now;
            long searched = Math.min(radius, Math.max(0L, ((long) ring * TILE - TILE / 2) * 4L));
            tell(message("Still searching for \"" + targetName + "\", checked up to " + searched + " of " + radius + " blocks away."));
        }

        private static int clamp(int value, int min, int max) {
            return value < min ? min : Math.min(value, max);
        }
    }
}
