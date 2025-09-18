package eaglemixins.client.particles;

import javax.annotation.Nullable;

// ParticleRule.java
public class ParticleRule {
    public int dimension; // Integer.MIN_VALUE == all
    @Nullable
    public java.util.Set<net.minecraft.world.biome.Biome> biomeSet; // null == all
    @Nullable public net.minecraftforge.common.BiomeDictionary.Type biomeTag; // overrides biomeSet

    public boolean scanBlocks;
    public boolean blocksAll; // true when blocks == []
    @Nullable public java.util.Set<net.minecraft.block.Block> blockSet; // when specific blocks

    public ParticleSpec[] particles;

    public double yRandom;
    public boolean blocksSolidAny;
    public int ticksBetweenRuns;
    public int maxPerRun;
    public float chance;
    public double yOffset;
    public double rise;
    public int range;
    public Tri canSeeSky;
    public int minY, maxY;
    public java.util.EnumSet<Weather> weather; // empty == ALL

    // internal
    int tickGate;

    public static final class ParticleSpec {
        public net.minecraft.util.EnumParticleTypes type;
        public boolean hasColor;
        public float r, g, b;
        public Float noteHue;
    }

    public enum Tri { TRUE, FALSE, ANY }
    public enum Weather { CLEAR, RAIN, THUNDER }
}
