package eaglemixins.client;

import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Spawns the Eye of Ender portal trail particle colored by the distance to the stronghold.
 */
@SideOnly(Side.CLIENT)
public class EnderEyeTrail {

    public static void spawnPortalParticle(double x, double y, double z, double vx, double vy, double vz, float distance) {
        Particle particle = Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(EnumParticleTypes.PORTAL.getParticleID(), x, y, z, vx, vy, vz);
        if (particle == null) return;

        // Vanilla portal particles are (0.9, 0.3, 1.0) * random brightness, the blue channel is that brightness
        float brightness = particle.getBlueColorF();
        float[] color = ForgeConfigHandler.strongholds.getEyeTrailColor(distance);
        particle.setRBGColorF(color[0] * brightness, color[1] * brightness, color[2] * brightness);
    }
}
