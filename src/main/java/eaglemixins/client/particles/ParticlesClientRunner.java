package eaglemixins.client.particles;

import eaglemixins.capability.ChunkRadiationSource;
import nc.capability.radiation.source.IRadiationSource;
import nc.radiation.RadiationHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ParticlesClientRunner {
    private static final Random RNG = new Random();
    private static List<ParticleRule> RULES = Collections.emptyList();

    public static void install(List<ParticleRule> rules) { RULES = rules; }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null) return;

        // Pause screen? bail early (don’t return inside the per-rule loop)
        if (mc.currentScreen instanceof GuiIngameMenu) return;

        final World w = mc.world;
        final EntityPlayerSP p = mc.player;
        final BlockPos pos = new BlockPos(p.posX, p.posY, p.posZ);
        int yPlayer = pos.getY();
        int dim = w.provider.getDimension();
        boolean isRaining = w.isRaining();
        boolean isThundering = w.isThundering();
        boolean sky = w.canSeeSky(pos);
        final Biome biome = w.getBiome(pos);
        Chunk chunk = w.getChunk(pos);
        IRadiationSource chunkSource = RadiationHelper.getRadiationSource(chunk);

        for (ParticleRule r : RULES) {
            // tick gate
            if ((r.tickGate = (r.tickGate + 1) % r.ticksBetweenRuns) != 0) continue;

            // dimension
            if (r.dimension != Integer.MIN_VALUE && r.dimension != dim) continue;

            // Y band (player Y; block Y validated per-hit below)
            if (yPlayer < r.minY || yPlayer > r.maxY) continue;

            // canSeeSky
            if (r.canSeeSky == ParticleRule.Tri.TRUE && !sky) continue;
            if (r.canSeeSky == ParticleRule.Tri.FALSE && sky) continue;

            if (chunkSource instanceof ChunkRadiationSource) {
                boolean rad_chunk = ((ChunkRadiationSource)chunkSource).hasHighRadiation(yPlayer / 16);
                if (!rad_chunk) continue;
            }

            // weather set (empty == ALL)
            if (!r.weather.isEmpty()) {
                boolean ok = r.weather.contains(ParticleRule.Weather.THUNDER) && isThundering;
                if (r.weather.contains(ParticleRule.Weather.RAIN) && isRaining && !isThundering) ok = true;
                if (r.weather.contains(ParticleRule.Weather.CLEAR) && !isRaining && !isThundering) ok = true;
                if (!ok) continue;
            }


            // biome/tag
            if (r.biomeTag != null) {
                if (!BiomeDictionary.hasType(biome, r.biomeTag)) continue;
            } else if (r.biomeSet != null && !r.biomeSet.contains(biome)) continue;

            int emitted = 0;

            if (!r.scanBlocks) {
                // Pure ambience: spawn around the player within range
                for (int tries = 0; emitted < r.maxPerRun && tries < r.maxPerRun * 2; tries++) {
                    if (RNG.nextFloat() > r.chance) continue;
                    spawnAroundPlayer(w, p, r);
                    emitted++;
                }
                continue;
            }

            // Block scan within range – switch to sampling for large ranges
            int R = r.range;
            int vHalf = Math.min(2, Math.max(1, R / 10)); // thin vertical band for perf
            int side = 2 * R + 1;
            int volume = side * side * (2 * vHalf + 1);

            if (volume <= 4096) {
                // small cube: iterate
                outer:
                for (int dx = -R; dx <= R; dx++) {
                    for (int dy = -vHalf; dy <= vHalf; dy++) {
                        for (int dz = -R; dz <= R; dz++) {
                            if (emitted >= r.maxPerRun) break outer;
                            if (RNG.nextFloat() > r.chance) continue;

                            BlockPos bp = pos.add(dx, dy, dz);
                            if (!w.isBlockLoaded(bp, false)) continue;

                            IBlockState s = w.getBlockState(bp);
                            Block b = s.getBlock();

                            if (!r.blocksAll && matchesBlockFilter(r, s, b)) continue;

                            double x = bp.getX() + 0.5 + (RNG.nextDouble() - 0.5) * 0.6;
                            double y0 = bp.getY() + r.yOffset + (r.yRandom > 0 ? RNG.nextDouble() * r.yRandom : 0.0);
                            double z = bp.getZ() + 0.5 + (RNG.nextDouble() - 0.5) * 0.6;
                            if (y0 < r.minY || y0 > r.maxY) continue;

                            spawnOneAt(w, s, x, y0, z, r);
                            emitted++;
                        }
                    }
                }
            } else {
                // large cube: random sampling
                int attempts = Math.min(volume, Math.max(64, r.maxPerRun * 20));
                for (int i = 0; i < attempts && emitted < r.maxPerRun; i++) {
                    if (RNG.nextFloat() > r.chance) continue;

                    int dx = RNG.nextInt(side) - R;
                    int dz = RNG.nextInt(side) - R;
                    int dy = RNG.nextInt(2 * vHalf + 1) - vHalf;

                    BlockPos bp = pos.add(dx, dy, dz);
                    if (!w.isBlockLoaded(bp, false)) continue;

                    IBlockState s = w.getBlockState(bp);
                    Block b = s.getBlock();

                    if (!r.blocksAll && matchesBlockFilter(r, s, b)) continue;

                    double x = bp.getX() + 0.5 + (RNG.nextDouble() - 0.5) * 0.6;
                    double y0 = bp.getY() + r.yOffset + (r.yRandom > 0 ? RNG.nextDouble() * r.yRandom : 0.0);
                    double z = bp.getZ() + 0.5 + (RNG.nextDouble() - 0.5) * 0.6;
                    if (y0 < r.minY || y0 > r.maxY) continue;

                    spawnOneAt(w, s, x, y0, z, r);
                    emitted++;
                }
            }
        }
    }


    private static boolean matchesBlockFilter(ParticleRule r,
                                              IBlockState s,
                                              Block b) {
        boolean ok = false;

        // "SOLID" wildcard (set by parser)
        if (r.blocksSolidAny) {
            // Cheap heuristics for 1.12 "solid"
            if (s.isFullBlock() || s.isOpaqueCube()) ok = true;
        }

        // Explicit block list (union with SOLID)
        if (!ok && r.blockSet != null && r.blockSet.contains(b)) ok = true;

        return !ok;
    }

    private static void spawnAroundPlayer(World w, EntityPlayer p, ParticleRule r) {
        double x = p.posX + (RNG.nextDouble() - 0.5) * (r.range * 2.0);
        double y = p.posY + r.yOffset + (r.yRandom > 0 ? RNG.nextDouble() * r.yRandom : 0.0);
        double z = p.posZ + (RNG.nextDouble() - 0.5) * (r.range * 2.0);
        spawnOneAt(w, null, x, y, z, r);
    }

    private static void spawnOneAt(World w,
                                   @Nullable IBlockState s,
                                   double x, double y, double z, ParticleRule r) {

        ParticleRule.ParticleSpec spec = r.particles[RNG.nextInt(r.particles.length)];

        // --- Vanilla types ---
        EnumParticleTypes type = spec.type; // may be null if using custom tokens
        if (type == null) return; // nothing to do

        switch (type) {
            case BLOCK_DUST:
            case BLOCK_CRACK: {
                int id = (s != null)
                        ? Block.getStateId(s)
                        : Block.getStateId(
                        w.getBlockState(new BlockPos(x, y - Math.max(0.0, r.yOffset), z)));
                w.spawnParticle(type, x, y, z, 0.0, r.rise, 0.0, id);
                break;
            }
            case REDSTONE: {
                float rr = spec.hasColor ? spec.r : 1.0f;
                float gg = spec.hasColor ? spec.g : 0.0f;
                float bb = spec.hasColor ? spec.b : 0.0f;
                if (rr <= 0f) rr = 0.001f; // redstone visibility quirk
                w.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, rr, gg, bb);
                break;
            }
            case SPELL_MOB:
            case SPELL_MOB_AMBIENT: {
                float rr = spec.hasColor ? spec.r : 0.6f;
                float gg = spec.hasColor ? spec.g : 0.7f;
                float bb = spec.hasColor ? spec.b : 0.4f;
                w.spawnParticle(type, x, y, z, rr, gg, bb);
                break;
            }
            case NOTE: {
                double hue = (spec.noteHue != null) ? spec.noteHue.doubleValue() : 0.5;
                w.spawnParticle(EnumParticleTypes.NOTE, x, y, z, hue, 0.0, 0.0);
                break;
            }
            default: {
                w.spawnParticle(type, x, y, z,
                        (RNG.nextDouble() - 0.5) * 0.02,
                        r.rise,
                        (RNG.nextDouble() - 0.5) * 0.02);
            }
        }
    }
}
