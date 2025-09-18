package eaglemixins.client.particles;

import net.minecraft.block.Block;
import java.util.*;

public final class ParticlesFactories {
    private ParticlesFactories() {}

    private static final Map<String, Set<Block>> BLOCK_SETS = new HashMap<>();
    private static final Map<String, List<ParticleRule.ParticleSpec>> PARTICLE_GROUPS = new HashMap<>();

    private static String norm(String id) { return id == null ? "" : id.toLowerCase(Locale.ROOT).trim(); }


    public static Set<Block> getBlockSet(String id) { return BLOCK_SETS.get(norm(id)); }


    public static List<ParticleRule.ParticleSpec> getParticleGroup(String id) {
        List<ParticleRule.ParticleSpec> g = PARTICLE_GROUPS.get(norm(id));
        if (g == null) return java.util.Collections.emptyList();
        List<ParticleRule.ParticleSpec> copy = new ArrayList<>(g.size());
        for (ParticleRule.ParticleSpec s : g) copy.add(copyOf(s));
        return copy;
    }


    private static ParticleRule.ParticleSpec copyOf(ParticleRule.ParticleSpec s) {
        ParticleRule.ParticleSpec c = new ParticleRule.ParticleSpec();
        c.type = s.type;
        c.hasColor = s.hasColor;
        c.r = s.r; c.g = s.g; c.b = s.b;
        c.noteHue = (s.noteHue == null ? null : s.noteHue);
        return c;
    }
}
