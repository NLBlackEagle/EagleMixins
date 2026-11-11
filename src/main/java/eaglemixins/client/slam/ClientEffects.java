package eaglemixins.client.slam;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.SRParasiteConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleCrit;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Client side of the Sentient Maul ground slam and the lance's red crits. Purely visual, nothing in the world changes. */
@SideOnly(Side.CLIENT)
public class ClientEffects {

    private static final List<Wave> waves = new ArrayList<>();
    private static final Random rand = new Random();
    private static final ParticleCrit.Factory critFactory = new ParticleCrit.Factory();

    // ---- Lance: red crit burst, like the vanilla crit emitter but red ----

    public static void redCrit(int entityId) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) return;
        Entity entity = mc.world.getEntityByID(entityId);
        if (entity == null) return;
        int tries = mc.gameSettings.particleSetting == 2 ? 16 : 48;
        for (int i = 0; i < tries; i++) {
            double dx = rand.nextFloat() * 2.0F - 1.0F;
            double dy = rand.nextFloat() * 2.0F - 1.0F;
            double dz = rand.nextFloat() * 2.0F - 1.0F;
            if (dx * dx + dy * dy + dz * dz > 1.0D) continue;
            double x = entity.posX + dx * entity.width / 4.0D;
            double y = entity.getEntityBoundingBox().minY + entity.height / 2.0F + dy * entity.height / 4.0D;
            double z = entity.posZ + dz * entity.width / 4.0D;
            Particle crit = critFactory.createParticle(EnumParticleTypes.CRIT.getParticleID(), mc.world, x, y, z, dx, dy + 0.2D, dz);
            if (crit == null) continue;
            crit.setRBGColorF(0.85F + rand.nextFloat() * 0.15F, 0.05F + rand.nextFloat() * 0.1F, 0.05F + rand.nextFloat() * 0.05F);
            mc.effectRenderer.addEffect(crit);
        }
    }

    // ---- Maul: ground slam ----

    public static void groundSlam(double x, double y, double z, float radius) {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.world;
        if (world == null || !ForgeConfigHandler.srparasites.maulSlam.showEffect) return;

        Wave wave = new Wave(world, x, y, z, radius);
        waves.add(wave);

        // Impact: burst of the ground's block dust flying outwards, a poof and dust that hangs around
        BlockPos ground = findSurface(world, MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
        if (ground == null) return;
        IBlockState state = world.getBlockState(ground);
        int stateId = Block.getStateId(state);
        int color = ParticleLingeringDust.colorOf(world, ground, state);
        double top = ground.getY() + 1.05D;
        boolean minimal = mc.gameSettings.particleSetting == 2;
        int burst = minimal ? 20 : 60;
        for (int i = 0; i < burst; i++) {
            double angle = rand.nextDouble() * Math.PI * 2.0D;
            double speed = 0.15D + rand.nextDouble() * 0.45D;
            world.spawnParticle(EnumParticleTypes.BLOCK_DUST, x, top, z,
                    Math.cos(angle) * speed, 0.1D + rand.nextDouble() * 0.35D, Math.sin(angle) * speed, stateId);
        }
        world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, x, top + 0.3D, z, 0.0D, 0.0D, 0.0D);
        int dust = minimal ? 4 : 14;
        for (int i = 0; i < dust; i++) {
            mc.effectRenderer.addEffect(new ParticleLingeringDust(world,
                    x + (rand.nextDouble() - 0.5D) * 2.0D, top + rand.nextDouble() * 0.5D, z + (rand.nextDouble() - 0.5D) * 2.0D, color));
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || waves.isEmpty()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) {
            waves.clear();
            return;
        }
        if (mc.isGamePaused()) return;
        Iterator<Wave> it = waves.iterator();
        while (it.hasNext()) {
            Wave wave = it.next();
            if (wave.world != mc.world || wave.tick()) it.remove();
        }
    }

    /** Topmost full block of the column near y, with a non-full block above it (what the wave pushes up) */
    static BlockPos findSurface(World world, int x, int y, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y + 3, z);
        if (!world.isBlockLoaded(pos)) return null;
        IBlockState above = world.getBlockState(pos);
        for (int dy = 2; dy >= -6; dy--) {
            pos.setY(y + dy);
            IBlockState state = world.getBlockState(pos);
            if (isPushable(state) && !above.isFullCube()) return pos.toImmutable();
            above = state;
        }
        return null;
    }

    private static boolean isPushable(IBlockState state) {
        return state.isFullCube() && state.getRenderType() == EnumBlockRenderType.MODEL && state.getMaterial().isSolid();
    }

    private static class Wave {
        private final World world;
        private final float radius;
        private final List<Column> columns = new ArrayList<>();
        private final SRParasiteConfig.MaulSlamConfig config = ForgeConfigHandler.srparasites.maulSlam;
        private float front = 0;
        private int next = 0;

        Wave(World world, double x, double y, double z, float radius) {
            this.world = world;
            this.radius = radius;
            int r = MathHelper.ceil(radius);
            int cx = MathHelper.floor(x), cy = MathHelper.floor(y), cz = MathHelper.floor(z);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    double bx = cx + dx + 0.5D - x, bz = cz + dz + 0.5D - z;
                    float dist = (float) Math.sqrt(bx * bx + bz * bz);
                    if (dist > radius) continue;
                    BlockPos surface = findSurface(world, cx + dx, cy, cz + dz);
                    if (surface != null) columns.add(new Column(surface, dist));
                }
            }
            columns.sort(Comparator.comparingDouble(c -> c.dist));
        }

        /** @return true when the wave is done */
        boolean tick() {
            front += config.waveSpeed;
            Minecraft mc = Minecraft.getMinecraft();
            boolean minimal = mc.gameSettings.particleSetting == 2;
            while (next < columns.size() && columns.get(next).dist <= front) {
                Column column = columns.get(next++);
                IBlockState state = world.getBlockState(column.pos);
                if (!isPushable(state)) continue;

                float falloff = 1.0F - 0.65F * column.dist / radius;
                float height = config.waveHeight * falloff * (0.8F + rand.nextFloat() * 0.4F);
                mc.effectRenderer.addEffect(new ParticleQuakeBlock(world, column.pos, state, height, 8 + rand.nextInt(3)));

                double px = column.pos.getX() + 0.5D, py = column.pos.getY() + 1.05D, pz = column.pos.getZ() + 0.5D;
                int stateId = Block.getStateId(state);
                for (int i = minimal ? 1 : 3; i > 0; i--) {
                    world.spawnParticle(EnumParticleTypes.BLOCK_DUST,
                            px + (rand.nextDouble() - 0.5D) * 0.8D, py, pz + (rand.nextDouble() - 0.5D) * 0.8D,
                            (rand.nextDouble() - 0.5D) * 0.15D, 0.1D + rand.nextDouble() * 0.2D, (rand.nextDouble() - 0.5D) * 0.15D, stateId);
                }
                if (rand.nextFloat() < (minimal ? config.dustChance * 0.25F : config.dustChance)) {
                    mc.effectRenderer.addEffect(new ParticleLingeringDust(world,
                            px + (rand.nextDouble() - 0.5D) * 0.8D, py + rand.nextDouble() * 0.3D, pz + (rand.nextDouble() - 0.5D) * 0.8D,
                            ParticleLingeringDust.colorOf(world, column.pos, state)));
                }
            }
            return next >= columns.size();
        }
    }

    private static class Column {
        final BlockPos pos;
        final float dist;

        Column(BlockPos pos, float dist) {
            this.pos = pos;
            this.dist = dist;
        }
    }
}
