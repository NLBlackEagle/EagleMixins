package eaglemixins.client.particles;

import net.minecraft.util.EnumParticleTypes;

// ParticlesClientRunner.java
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(value = net.minecraftforge.fml.relauncher.Side.CLIENT)
public class ParticlesClientRunner {
    private static final java.util.Random RNG = new java.util.Random();
    private static java.util.List<ParticleRule> RULES = java.util.Collections.emptyList();

    public static void install(java.util.List<ParticleRule> rules) { RULES = rules; }

    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public static void onClientTick(net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent e) {
        if (e.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) return;

        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null) return;

        // Pause screen? bail early (don’t return inside the per-rule loop)
        if (mc.currentScreen instanceof net.minecraft.client.gui.GuiIngameMenu) return;

        final net.minecraft.world.World w = mc.world;
        final net.minecraft.client.entity.EntityPlayerSP p = mc.player;
        final net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(p.posX, p.posY, p.posZ);
        final int yPlayer = pos.getY();
        final int dim = w.provider.getDimension();
        final boolean isRaining = w.isRaining();
        final boolean isThundering = w.isThundering();
        final boolean sky = w.canSeeSky(pos);
        final net.minecraft.world.biome.Biome biome = w.getBiome(pos);

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

            // weather set (empty == ALL)
            if (!r.weather.isEmpty()) {
                boolean ok = r.weather.contains(ParticleRule.Weather.THUNDER) && isThundering;
                if (r.weather.contains(ParticleRule.Weather.RAIN) && isRaining && !isThundering) ok = true;
                if (r.weather.contains(ParticleRule.Weather.CLEAR) && !isRaining && !isThundering) ok = true;
                if (!ok) continue;
            }

            // biome/tag
            if (r.biomeTag != null) {
                if (!net.minecraftforge.common.BiomeDictionary.hasType(biome, r.biomeTag)) continue;
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
            final int R = r.range;
            final int vHalf = Math.min(2, Math.max(1, R / 10)); // thin vertical band for perf
            final int side = 2 * R + 1;
            final int volume = side * side * (2 * vHalf + 1);

            if (volume <= 4096) {
                // small cube: iterate
                outer:
                for (int dx = -R; dx <= R; dx++) {
                    for (int dy = -vHalf; dy <= vHalf; dy++) {
                        for (int dz = -R; dz <= R; dz++) {
                            if (emitted >= r.maxPerRun) break outer;
                            if (RNG.nextFloat() > r.chance) continue;

                            net.minecraft.util.math.BlockPos bp = pos.add(dx, dy, dz);
                            if (!w.isBlockLoaded(bp, false)) continue;

                            net.minecraft.block.state.IBlockState s = w.getBlockState(bp);
                            net.minecraft.block.Block b = s.getBlock();

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

                    net.minecraft.util.math.BlockPos bp = pos.add(dx, dy, dz);
                    if (!w.isBlockLoaded(bp, false)) continue;

                    net.minecraft.block.state.IBlockState s = w.getBlockState(bp);
                    net.minecraft.block.Block b = s.getBlock();

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
                                              net.minecraft.block.state.IBlockState s,
                                              net.minecraft.block.Block b) {
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

    private static void spawnAroundPlayer(net.minecraft.world.World w, net.minecraft.entity.player.EntityPlayer p, ParticleRule r) {
        double x = p.posX + (RNG.nextDouble() - 0.5) * (r.range * 2.0);
        double y = p.posY + r.yOffset + (r.yRandom > 0 ? RNG.nextDouble() * r.yRandom : 0.0);
        double z = p.posZ + (RNG.nextDouble() - 0.5) * (r.range * 2.0);
        spawnOneAt(w, null, x, y, z, r);
    }

    private static void spawnOneAt(net.minecraft.world.World w,
                                   @javax.annotation.Nullable net.minecraft.block.state.IBlockState s,
                                   double x, double y, double z, ParticleRule r) {

        ParticleRule.ParticleSpec spec = r.particles[RNG.nextInt(r.particles.length)];

        // --- Vanilla types ---
        EnumParticleTypes type = spec.type; // may be null if using custom tokens
        if (type == null) return; // nothing to do

        switch (type) {
            case BLOCK_DUST:
            case BLOCK_CRACK: {
                int id = (s != null)
                        ? net.minecraft.block.Block.getStateId(s)
                        : net.minecraft.block.Block.getStateId(
                        w.getBlockState(new net.minecraft.util.math.BlockPos(x, y - Math.max(0.0, r.yOffset), z)));
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
