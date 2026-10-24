package eaglemixins.client;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.LightningConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Purely visual lightning: red/black bolts shooting diagonally through the Underneath, black/dark yellow strikes in the nuclear wasteland,
 * red/black strikes in parasite biomes, purple/black bolts shooting diagonally through the End
 */
public class AmbientLightningHandler {
    private static final Random RNG = new Random();
    private static final List<Bolt> BOLTS = new ArrayList<>();
    private static final int LIFE = 10;

    private static class Bolt {
        final List<Vec3d> segments = new ArrayList<>(); // start/end pairs
        final List<Float> widths = new ArrayList<>();   // per segment, branches are thinner
        final int dimmedTicks = RNG.nextInt(1 << LIFE);   // ticks where the bolt flickers dimmer, like vanilla's
        final float flash;                                // fog flash strength, less for far bolts
        final float r, g, b;
        final boolean dark;                               // dark colors can't glow, they're drawn over the world instead
        int age = 0;

        Bolt(float flash, int color) {
            this.flash = flash;
            this.r = (color >> 16 & 255) / 255F;
            this.g = (color >> 8 & 255) / 255F;
            this.b = (color & 255) / 255F;
            this.dark = 0.299F * r + 0.587F * g + 0.114F * b < 0.3F;
        }

        float strength(float partialTicks) {
            float s = 1 - (age + partialTicks) / LIFE;
            if (age > 1 && (dimmedTicks & (1 << age)) != 0) s *= 0.35F;
            return Math.max(0, s);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null) {
            BOLTS.clear();
            return;
        }
        if (mc.isGamePaused()) return;

        BOLTS.removeIf(bolt -> ++bolt.age >= LIFE);

        LightningConfig config = ForgeConfigHandler.lightning;
        int dimension = mc.world.provider.getDimension();
        if (config.underneathLightning && dimension == config.underneathDimension) {
            // a few tries so spots in solid rock or right against a wall don't make bolts rarer than the interval
            if (RNG.nextDouble() * config.underneathInterval * 20 < 1)
                for (int i = 0; i < 8 && !spawnDiagonalBolt(mc.world, mc.player, config.underneathMinDistance,
                        config.underneathMaxDistance, config.getUnderneathColors(), config.underneathVolume, 1.0F); i++);
        } else if (config.endLightning && dimension == config.endDimension) {
            // far away over the void, so they're bigger and higher up to still be seen
            if (RNG.nextDouble() * config.endInterval * 20 < 1)
                for (int i = 0; i < 8 && !spawnDiagonalBolt(mc.world, mc.player, config.endMinDistance,
                        config.endMaxDistance, config.getEndColors(), config.endVolume, 3.0F); i++);
        } else {
            ResourceLocation biome = mc.world.getBiome(mc.player.getPosition()).getRegistryName();
            if (config.wastelandLightning && config.isWastelandBiome(biome)) {
                if (RNG.nextDouble() * config.wastelandInterval * 20 < 1)
                    for (int i = 0; i < 8 && !spawnSkyBolt(mc.world, mc.player, config::isWastelandBiome, config.wastelandMinDistance,
                            config.wastelandMaxDistance, config.getWastelandColors(), config.wastelandVolume); i++);
            } else if (config.parasiteLightning && config.isParasiteBiome(biome)) {
                if (RNG.nextDouble() * config.parasiteInterval * 20 < 1)
                    for (int i = 0; i < 8 && !spawnSkyBolt(mc.world, mc.player, config::isParasiteBiome, config.parasiteMinDistance,
                            config.parasiteMaxDistance, config.getParasiteColors(), config.parasiteVolume); i++);
            }
        }
    }

    // Underneath/End: from somewhere in the air above the player's height diagonally down, stopping where it hits a block
    private static boolean spawnDiagonalBolt(World world, EntityPlayer player, int minDistance, int maxDistance, int[] colors, float volume, float size) {
        int max = Math.max(minDistance + 1, maxDistance);
        double dist = minDistance + RNG.nextDouble() * (max - minDistance);
        double angle = RNG.nextDouble() * Math.PI * 2;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(new BlockPos(
                player.posX + Math.cos(angle) * dist, Math.min(250, player.posY + 8 * size + RNG.nextInt((int) (32 * size))), player.posZ + Math.sin(angle) * dist));
        if (!world.isBlockLoaded(pos)) return false;
        int tries = 0;
        while (!world.isAirBlock(pos) && tries++ < 48 && pos.getY() > 1) pos.move(EnumFacing.DOWN);
        if (!world.isAirBlock(pos)) return false;
        Vec3d start = new Vec3d(pos.getX() + RNG.nextDouble(), pos.getY() + RNG.nextDouble(), pos.getZ() + RNG.nextDouble());

        double slope = 0.6 + RNG.nextDouble() * 0.9;
        return strike(world, start, slope, (20 + RNG.nextDouble() * 30) * size, (float) Math.max(0.15, 1 - dist / max), colors[RNG.nextInt(colors.length)], volume, 0.5F, size);
    }

    // Wasteland/parasite biomes: from high up in the sky (nearly) straight down onto the ground, only where the ground is in one of the biomes too
    private static boolean spawnSkyBolt(World world, EntityPlayer player, Predicate<ResourceLocation> isBiome, int minDistance, int maxDistance, int[] colors, float volume) {
        int max = Math.max(minDistance + 1, maxDistance);
        double dist = minDistance + RNG.nextDouble() * (max - minDistance);
        double angle = RNG.nextDouble() * Math.PI * 2;

        BlockPos ground = new BlockPos(player.posX + Math.cos(angle) * dist, 0, player.posZ + Math.sin(angle) * dist);
        if (!world.isBlockLoaded(ground)) return false;
        ground = world.getPrecipitationHeight(ground);
        if (!isBiome.test(world.getBiome(ground).getRegistryName())) return false;

        return skyBoltAt(world, new Vec3d(ground.getX() + 0.5, ground.getY(), ground.getZ() + 0.5), (float) Math.max(0.15, 1 - dist / max), colors, volume);
    }

    /** Wasteland bolt from high up in the sky onto target, used for the ambient ones and to replace real lightning */
    public static boolean skyBoltAt(World world, Vec3d target, float flash, float volume) {
        return skyBoltAt(world, target, flash, ForgeConfigHandler.lightning.getWastelandColors(), volume);
    }

    private static boolean skyBoltAt(World world, Vec3d target, float flash, int[] colors, float volume) {
        // Start up high, leaning away from where it lands so the bolt still hits about there
        double slope = RNG.nextDouble() * 0.35;
        double height = 60 + RNG.nextDouble() * 50;
        double lean = RNG.nextDouble() * Math.PI * 2;
        Vec3d start = new Vec3d(target.x - Math.cos(lean) * slope * height, Math.min(320, target.y + height), target.z - Math.sin(lean) * slope * height);

        return strike(world, start, new Vec3d(Math.cos(lean) * slope, -1, Math.sin(lean) * slope), height * 1.2, flash,
                colors[RNG.nextInt(colors.length)], volume, 0.7F, 1.0F);
    }

    private static boolean strike(World world, Vec3d start, double slope, double length, float flash, int color, float volume, float pitch, float width) {
        double dirAngle = RNG.nextDouble() * Math.PI * 2;
        return strike(world, start, new Vec3d(Math.cos(dirAngle) * slope, -1, Math.sin(dirAngle) * slope), length, flash, color, volume, pitch, width);
    }

    private static boolean strike(World world, Vec3d start, Vec3d dir, double maxLength, float flash, int color, float volume, float pitch, float width) {
        dir = dir.normalize();
        Vec3d end = start.add(dir.scale(maxLength));
        @Nullable RayTraceResult hit = world.rayTraceBlocks(start, end, false, true, false);
        if (hit != null && hit.hitVec != null) end = hit.hitVec;
        double length = end.distanceTo(start);
        if (length < 6) return false;

        Bolt bolt = new Bolt(flash, color);
        addJagged(bolt, start, end, width);
        int branches = 1 + RNG.nextInt(4);
        for (int i = 0; i < branches; i++) {
            Vec3d from = start.add(end.subtract(start).scale(0.15 + RNG.nextDouble() * 0.6));
            Vec3d branchDir = dir.add((RNG.nextDouble() - 0.5) * 1.2, (RNG.nextDouble() - 0.5) * 0.6, (RNG.nextDouble() - 0.5) * 1.2).normalize();
            addJagged(bolt, from, from.add(branchDir.scale(length * (0.15 + RNG.nextDouble() * 0.25))), 0.55F * width);
        }
        BOLTS.add(bolt);

        if (volume > 0) {
            world.playSound(start.x, start.y, start.z, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.WEATHER, volume, pitch + RNG.nextFloat() * 0.25F, false);
            if (hit != null) world.playSound(end.x, end.y, end.z, SoundEvents.ENTITY_LIGHTNING_IMPACT, SoundCategory.WEATHER, volume / 3, pitch + 0.1F + RNG.nextFloat() * 0.2F, false);
        }
        return true;
    }

    private static void addJagged(Bolt bolt, Vec3d from, Vec3d to, float width) {
        Vec3d line = to.subtract(from);
        int steps = Math.max(2, (int) (line.length() / 2));
        Vec3d prev = from;
        for (int i = 1; i <= steps; i++) {
            Vec3d next = from.add(line.scale((double) i / steps));
            if (i < steps) next = next.add((RNG.nextDouble() - 0.5) * 1.6, (RNG.nextDouble() - 0.5) * 1.6, (RNG.nextDouble() - 0.5) * 1.6);
            bolt.segments.add(prev);
            bolt.segments.add(next);
            bolt.widths.add(width);
            prev = next;
        }
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        if (BOLTS.isEmpty()) return;
        RenderManager rm = Minecraft.getMinecraft().getRenderManager();
        Vec3d cam = new Vec3d(rm.viewerPosX, rm.viewerPosY, rm.viewerPosZ);

        GlStateManager.pushMatrix();
        GlStateManager.translate(-cam.x, -cam.y, -cam.z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
        GlStateManager.disableFog();
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);

        // glowing bolts add their light, dark ones are blended over the world
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        renderBolts(cam, event.getPartialTicks(), false);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        renderBolts(cam, event.getPartialTicks(), true);

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        if (fog) GlStateManager.enableFog();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private static void renderBolts(Vec3d cam, float partialTicks, boolean dark) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (Bolt bolt : BOLTS) {
            if (bolt.dark != dark) continue;
            float s = bolt.strength(partialTicks);
            if (s <= 0) continue;
            // dark bolts keep their color in the core, glowing ones get a whitish core
            float cr = dark ? bolt.r : (bolt.r + 2) / 3, cg = dark ? bolt.g : (bolt.g + 2) / 3, cb = dark ? bolt.b : (bolt.b + 2) / 3;
            for (int i = 0; i < bolt.widths.size(); i++) {
                Vec3d from = bolt.segments.get(2 * i), to = bolt.segments.get(2 * i + 1);
                float w = bolt.widths.get(i);
                addSegment(buffer, cam, from, to, 0.45F * w, bolt.r, bolt.g, bolt.b, (dark ? 0.15F : 0.2F) * s);
                addSegment(buffer, cam, from, to, 0.18F * w, bolt.r, bolt.g, bolt.b, (dark ? 0.45F : 0.55F) * s);
                addSegment(buffer, cam, from, to, 0.06F * w, cr, cg, cb, (dark ? 1.0F : 0.9F) * s);
            }
        }
        tessellator.draw();
    }

    // Quad along the segment, turned to face the camera
    private static void addSegment(BufferBuilder buffer, Vec3d cam, Vec3d from, Vec3d to, float width, float r, float g, float b, float a) {
        Vec3d side = to.subtract(from).crossProduct(cam.subtract(from)).normalize().scale(width);
        buffer.pos(from.x + side.x, from.y + side.y, from.z + side.z).color(r, g, b, a).endVertex();
        buffer.pos(from.x - side.x, from.y - side.y, from.z - side.z).color(r, g, b, a).endVertex();
        buffer.pos(to.x - side.x, to.y - side.y, to.z - side.z).color(r, g, b, a).endVertex();
        buffer.pos(to.x + side.x, to.y + side.y, to.z + side.z).color(r, g, b, a).endVertex();
    }

    // Briefly tints the fog with the bolt color: a colored flash for glowing bolts, a darkening for dark ones
    @SubscribeEvent
    public static void onFogColors(EntityViewRenderEvent.FogColors event) {
        Bolt strongest = null;
        float flash = 0;
        for (Bolt bolt : BOLTS) {
            float f = bolt.flash * bolt.strength((float) event.getRenderPartialTicks());
            if (f > flash) {
                flash = f;
                strongest = bolt;
            }
        }
        if (strongest == null) return;
        flash *= 0.35F;
        event.setRed(event.getRed() + (strongest.r - event.getRed()) * flash);
        event.setGreen(event.getGreen() + (strongest.g - event.getGreen()) * flash);
        event.setBlue(event.getBlue() + (strongest.b - event.getBlue()) * flash);
    }
}
