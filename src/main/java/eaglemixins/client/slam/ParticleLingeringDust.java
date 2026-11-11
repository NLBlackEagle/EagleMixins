package eaglemixins.client.slam;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Small dust puff in a block's color that slowly drifts and hangs around for a few seconds before fading */
@SideOnly(Side.CLIENT)
public class ParticleLingeringDust extends Particle {

    private static final int FALLBACK_COLOR = 0x8B7D6B;

    public ParticleLingeringDust(World world, double x, double y, double z, int color) {
        super(world, x, y, z);
        motionX = (rand.nextDouble() - 0.5D) * 0.05D;
        motionY = 0.005D + rand.nextDouble() * 0.025D;
        motionZ = (rand.nextDouble() - 0.5D) * 0.05D;
        float brightness = 0.8F + rand.nextFloat() * 0.3F;
        setRBGColorF(Math.min(1.0F, (color >> 16 & 255) / 255.0F * brightness),
                Math.min(1.0F, (color >> 8 & 255) / 255.0F * brightness),
                Math.min(1.0F, (color & 255) / 255.0F * brightness));
        particleScale *= 0.5F + rand.nextFloat() * 0.6F;
        particleMaxAge = 50 + rand.nextInt(60);
        canCollide = true;
        setParticleTextureIndex(7);
    }

    /** The block's map color, close enough to its texture and cheap */
    public static int colorOf(World world, BlockPos pos, IBlockState state) {
        try {
            MapColor mapColor = state.getMapColor(world, pos);
            if (mapColor != null && mapColor != MapColor.AIR && mapColor.colorValue != 0) return mapColor.colorValue;
        } catch (Exception ignored) {
        }
        return FALLBACK_COLOR;
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge) {
            setExpired();
            return;
        }
        setParticleTextureIndex(7 - particleAge * 8 / (particleMaxAge + 1));
        float fadeStart = particleMaxAge * 0.6F;
        if (particleAge > fadeStart) setAlphaF(1.0F - (particleAge - fadeStart) / (particleMaxAge - fadeStart));

        motionY += 0.0006D;
        move(motionX, motionY, motionZ);
        motionX *= 0.93D;
        motionY *= 0.95D;
        motionZ *= 0.93D;
    }
}
