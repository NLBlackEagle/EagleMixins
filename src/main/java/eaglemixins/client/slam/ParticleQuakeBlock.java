package eaglemixins.client.slam;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * A copy of a block that bobs up out of the ground and back, drawn over the real block (which stays untouched).
 * Neighbouring columns bobbing one after the other make the earthquake wave.
 */
@SideOnly(Side.CLIENT)
public class ParticleQuakeBlock extends Particle {

    private static final double GROW = 0.01D; // drawn slightly bigger than the real block so their faces don't flicker
    private static final float[] SHADE = {0.5F, 1.0F, 0.8F, 0.8F, 0.6F, 0.6F}; // vanilla face shading, EnumFacing order

    private final BlockPos pos;
    private final float height;
    private final TextureAtlasSprite[] sprites = new TextureAtlasSprite[6];
    private final int[] tints = new int[6];

    public ParticleQuakeBlock(World world, BlockPos pos, IBlockState state, float height, int life) {
        super(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        this.pos = pos;
        this.height = height;
        this.particleMaxAge = life;
        this.motionX = this.motionY = this.motionZ = 0;
        this.canCollide = false;
        setupFaces(world, pos, state);
    }

    private void setupFaces(World world, BlockPos pos, IBlockState state) {
        Minecraft mc = Minecraft.getMinecraft();
        TextureAtlasSprite fallback = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
        IBlockState actual = state;
        IBakedModel model = null;
        try {
            actual = state.getActualState(world, pos);
            model = mc.getBlockRendererDispatcher().getModelForState(actual);
        } catch (Exception ignored) {
        }
        for (EnumFacing face : EnumFacing.values()) {
            sprites[face.getIndex()] = fallback;
            tints[face.getIndex()] = -1;
            if (model == null) continue;
            try {
                BakedQuad quad = firstQuad(model.getQuads(actual, face, 0L), face);
                if (quad == null) quad = firstQuad(model.getQuads(actual, null, 0L), face);
                if (quad == null) continue;
                sprites[face.getIndex()] = quad.getSprite();
                if (quad.hasTintIndex()) tints[face.getIndex()] = mc.getBlockColors().colorMultiplier(actual, world, pos, quad.getTintIndex());
            } catch (Exception ignored) {
                // some modded models need extended states, the particle texture will do
            }
        }
    }

    private static BakedQuad firstQuad(List<BakedQuad> quads, EnumFacing face) {
        for (BakedQuad quad : quads) if (quad.getFace() == face) return quad;
        return null;
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge) setExpired();
    }

    @Override
    public int getFXLayer() {
        return 1; // block texture atlas
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        BlockPos up = pos.up();
        return world.isBlockLoaded(up) ? world.getCombinedLight(up, 0) : 0;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float t = Math.min(1.0F, (particleAge + partialTicks) / particleMaxAge);
        double lift = MathHelper.sin(t * (float) Math.PI) * height;
        if (lift < 0.02D) return;

        double x0 = pos.getX() - interpPosX - GROW, x1 = pos.getX() + 1 - interpPosX + GROW;
        double y0 = pos.getY() + lift - interpPosY - GROW, y1 = pos.getY() + 1 + lift - interpPosY + GROW;
        double z0 = pos.getZ() - interpPosZ - GROW, z1 = pos.getZ() + 1 - interpPosZ + GROW;
        int light = getBrightnessForRender(partialTicks);
        int sky = light >> 16 & 65535, block = light & 65535;

        face(buffer, EnumFacing.UP, sky, block, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
        face(buffer, EnumFacing.NORTH, sky, block, x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0);
        face(buffer, EnumFacing.SOUTH, sky, block, x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1);
        face(buffer, EnumFacing.WEST, sky, block, x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1);
        face(buffer, EnumFacing.EAST, sky, block, x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0);
    }

    /** Corners go top-left, bottom-left, bottom-right, top-right seen from outside the block */
    private void face(BufferBuilder buffer, EnumFacing face, int sky, int block,
                      double ax, double ay, double az, double bx, double by, double bz,
                      double cx, double cy, double cz, double dx, double dy, double dz) {
        TextureAtlasSprite sprite = sprites[face.getIndex()];
        int tint = tints[face.getIndex()];
        float shade = SHADE[face.getIndex()];
        float r = shade, g = shade, b = shade;
        if (tint != -1) {
            r *= (tint >> 16 & 255) / 255.0F;
            g *= (tint >> 8 & 255) / 255.0F;
            b *= (tint & 255) / 255.0F;
        }
        float u0 = sprite.getMinU(), u1 = sprite.getMaxU(), v0 = sprite.getMinV(), v1 = sprite.getMaxV();
        buffer.pos(ax, ay, az).tex(u0, v0).color(r, g, b, 1.0F).lightmap(sky, block).endVertex();
        buffer.pos(bx, by, bz).tex(u0, v1).color(r, g, b, 1.0F).lightmap(sky, block).endVertex();
        buffer.pos(cx, cy, cz).tex(u1, v1).color(r, g, b, 1.0F).lightmap(sky, block).endVertex();
        buffer.pos(dx, dy, dz).tex(u1, v0).color(r, g, b, 1.0F).lightmap(sky, block).endVertex();
    }
}
