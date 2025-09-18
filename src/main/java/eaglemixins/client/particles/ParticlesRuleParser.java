package eaglemixins.client.particles;

import net.minecraft.util.EnumParticleTypes;


// (keep your imports; only these two are new short imports)

public class ParticlesRuleParser {
    private ParticlesRuleParser() {}

    // ==== NEW: public helper so factories can build ParticleSpecs from the same token syntax ====
    public static ParticleRule.ParticleSpec parseParticleSpec(String token) {
        String name = token;
        String arg  = null;
        int at = token.indexOf('@');
        if (at >= 0) {
            name = token.substring(0, at).trim();
            arg  = token.substring(at + 1).trim();
        }

        ParticleRule.ParticleSpec spec = new ParticleRule.ParticleSpec();
        spec.type = resolveParticle(name); // accepts enum or /particle command name

        if (arg != null && !arg.isEmpty()) {
            if (spec.type == EnumParticleTypes.NOTE) {
                try {
                    spec.noteHue = clamp01(Float.parseFloat(arg));
                } catch (NumberFormatException ignored) {}
            } else if (spec.type == EnumParticleTypes.REDSTONE
                    || spec.type == EnumParticleTypes.SPELL_MOB
                    || spec.type == EnumParticleTypes.SPELL_MOB_AMBIENT) {
                float[] rgb = parseColor(arg); // normalized 0..1
                spec.hasColor = true;
                spec.r = rgb[0]; spec.g = rgb[1]; spec.b = rgb[2];
                if (spec.type == EnumParticleTypes.REDSTONE && spec.r <= 0f) spec.r = 0.001f;
            }
        }
        return spec;
    }
    // ==== END NEW ====

    public static java.util.List<ParticleRule> parse(String[] lines) {
        java.util.ArrayList<ParticleRule> out = new java.util.ArrayList<>();
        if (lines == null) return out;
        int ln = 0;
        for (String raw : lines) {
            ln++;
            if (raw == null) continue;
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            try {
                out.add(parseOne(line));
            } catch (Exception ex) {
                net.minecraftforge.fml.common.FMLLog.log.warn("[EagleMixins][Particles] Bad rule line {}: {}", ln, line, ex);
            }
        }
        return out;
    }

    private static ParticleRule parseOne(String s) {
        java.util.List<String> f = splitTopCSV(s);
        if (f.size() != 15 && f.size() != 16)
            throw new IllegalArgumentException("Expected 15 or 16 fields, got " + f.size());

        final boolean hasYRand = (f.size() == 16);
        ParticleRule r = new ParticleRule();

        // 0 dimension
        String dim = f.get(0).trim();
        r.dimension = dim.equalsIgnoreCase("all") ? Integer.MIN_VALUE : Integer.parseInt(dim);

        // 1 biome id (overridden by tag later)
        String biomeId = f.get(1).trim();
        if (!biomeId.equalsIgnoreCase("all")) {
            net.minecraft.util.ResourceLocation rl = new net.minecraft.util.ResourceLocation(biomeId);
            net.minecraft.world.biome.Biome b = net.minecraftforge.fml.common.registry.ForgeRegistries.BIOMES.getValue(rl);
            if (b != null) r.biomeSet = java.util.Collections.singleton(b);
        }

        // 2 blocks array  (supports IDs, factory#<id>, and SOLID)
        java.util.List<String> blocks = parseArray(f.get(2));
        if (blocks.isEmpty()) { // [] => all blocks
            r.scanBlocks = true; r.blocksAll = true; r.blockSet = null; r.blocksSolidAny = false;
        } else if (blocks.size() == 1 && (eq(blocks.get(0), "none") || eq(blocks.get(0), "-"))) {
            r.scanBlocks = false; r.blocksAll = false; r.blockSet = null; r.blocksSolidAny = false;
        } else {
            r.scanBlocks = true;
            if (blocks.size() == 1 && eq(blocks.get(0), "all")) {
                r.blocksAll = true; r.blockSet = null; r.blocksSolidAny = false;
            } else {
                r.blocksAll = false;
                java.util.HashSet<net.minecraft.block.Block> set = new java.util.HashSet<>();
                boolean sawSolid = false;
                for (String tok : blocks) {
                    String id = tok.trim();
                    if (eq(id, "solid")) { sawSolid = true; continue; }
                    if (id.regionMatches(true, 0, "factory#", 0, 8)) {
                        String fid = id.substring(8).trim();
                        java.util.Set<net.minecraft.block.Block> group = ParticlesFactories.getBlockSet(fid);
                        if (group == null || group.isEmpty()) {
                            net.minecraftforge.fml.common.FMLLog.log.warn("[EagleMixins][Particles] Unknown or empty block factory '{}'", fid);
                        } else {
                            set.addAll(group);
                        }
                        continue;
                    }
                    net.minecraft.block.Block blk = net.minecraft.block.Block.getBlockFromName(id);
                    if (blk != null) set.add(blk);
                    else net.minecraftforge.fml.common.FMLLog.log.warn("[EagleMixins][Particles] Unknown block id '{}'", id);
                }
                r.blockSet = set.isEmpty() ? null : set;
                r.blocksSolidAny = sawSolid;
            }
        }

        // 3 particles array (supports TYPE, TYPE@color, factory#<id>, and your custom tokens via parseParticleSpec)
        java.util.List<String> parts = parseArray(f.get(3));
        if (parts.isEmpty()) throw new IllegalArgumentException("Particles array may not be empty");
        java.util.ArrayList<ParticleRule.ParticleSpec> pl = new java.util.ArrayList<>(parts.size());
        for (String token : parts) {
            String t = token.trim();
            if (t.regionMatches(true, 0, "factory#", 0, 8)) {
                String fid = t.substring(8).trim();
                java.util.List<ParticleRule.ParticleSpec> group = ParticlesFactories.getParticleGroup(fid);
                if (group.isEmpty())
                    throw new IllegalArgumentException("Unknown or empty particle factory: " + fid);
                pl.addAll(group);
                continue;
            }
            pl.add(parseParticleSpec(t)); // must exist (handles vanilla + RADDUST, etc.)
        }
        r.particles = pl.toArray(new ParticleRule.ParticleSpec[0]);

        // 4.. end
        r.ticksBetweenRuns = Math.max(1, Integer.parseInt(f.get(4).trim()));
        r.maxPerRun        = Math.max(1, Integer.parseInt(f.get(5).trim()));
        r.chance           = clamp01(Float.parseFloat(f.get(6).trim()));
        r.yOffset          = Double.parseDouble(f.get(7).trim());

        if (hasYRand) {
            r.yRandom     = Math.max(0d, Double.parseDouble(f.get(8).trim()));    // NEW field
            r.rise        = Double.parseDouble(f.get(9).trim());
            r.range       = Math.max(1, Integer.parseInt(f.get(10).trim()));

            String sky    = f.get(11).trim().toLowerCase();
            r.canSeeSky   = "true".equals(sky) ? ParticleRule.Tri.TRUE :
                    "false".equals(sky) ? ParticleRule.Tri.FALSE : ParticleRule.Tri.ANY;

            r.minY        = Integer.parseInt(f.get(12).trim());
            r.maxY        = Integer.parseInt(f.get(13).trim());

            // 14 weather
            java.util.List<String> we = parseArray(f.get(14));
            if (we.isEmpty()) {
                r.weather = java.util.EnumSet.noneOf(ParticleRule.Weather.class);
            } else {
                r.weather = java.util.EnumSet.noneOf(ParticleRule.Weather.class);
                for (String w : we) {
                    String tt = w.trim().toUpperCase();
                    switch (tt) {
                        case "CLEAR":
                            r.weather.add(ParticleRule.Weather.CLEAR);
                            break;
                        case "RAIN":
                            r.weather.add(ParticleRule.Weather.RAIN);
                            break;
                        case "THUNDER":
                            r.weather.add(ParticleRule.Weather.THUNDER);
                            break;
                    }
                }
            }

            // 15 tag
            String tag = f.get(15).trim();
            if (!tag.isEmpty() && !eq(tag, "all")) {
                r.biomeTag = net.minecraftforge.common.BiomeDictionary.Type.getType(tag.toUpperCase());
            }
        } else {
            // Back-compat (15 fields)
            r.yRandom     = 0d;
            r.rise        = Double.parseDouble(f.get(8).trim());
            r.range       = Math.max(1, Integer.parseInt(f.get(9).trim()));

            String sky    = f.get(10).trim().toLowerCase();
            r.canSeeSky   = "true".equals(sky) ? ParticleRule.Tri.TRUE :
                    "false".equals(sky) ? ParticleRule.Tri.FALSE : ParticleRule.Tri.ANY;

            r.minY        = Integer.parseInt(f.get(11).trim());
            r.maxY        = Integer.parseInt(f.get(12).trim());

            java.util.List<String> we = parseArray(f.get(13));
            if (we.isEmpty()) {
                r.weather = java.util.EnumSet.noneOf(ParticleRule.Weather.class);
            } else {
                r.weather = java.util.EnumSet.noneOf(ParticleRule.Weather.class);
                for (String w : we) {
                    String tt = w.trim().toUpperCase();
                    switch (tt) {
                        case "CLEAR":
                            r.weather.add(ParticleRule.Weather.CLEAR);
                            break;
                        case "RAIN":
                            r.weather.add(ParticleRule.Weather.RAIN);
                            break;
                        case "THUNDER":
                            r.weather.add(ParticleRule.Weather.THUNDER);
                            break;
                    }
                }
            }

            String tag = f.get(14).trim();
            if (!tag.isEmpty() && !eq(tag, "all")) {
                r.biomeTag = net.minecraftforge.common.BiomeDictionary.Type.getType(tag.toUpperCase());
            }
        }

        return r;
    }

    private static EnumParticleTypes resolveParticle(String s) {
        String want = s.trim();
        for (EnumParticleTypes t : EnumParticleTypes.values()) {
            if (t.name().equalsIgnoreCase(want) || t.getParticleName().equalsIgnoreCase(want)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown particle: " + s);
    }

    private static boolean eq(String a, String b) { return a.equalsIgnoreCase(b); }
    private static float clamp01(float v) { return v < 0f ? 0f : (Math.min(v, 1f)); }

    private static java.util.List<String> splitTopCSV(String s) {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int depth = 0;
        for (int i=0;i<s.length();i++) {
            char c = s.charAt(i);
            if (c=='[') depth++;
            if (c==']' && depth>0) depth--;
            if (c==',' && depth==0) { out.add(cur.toString()); cur.setLength(0); }
            else cur.append(c);
        }
        out.add(cur.toString());
        return out;
    }

    private static java.util.List<String> parseArray(String tok) {
        String t = tok.trim();
        if (!t.startsWith("[") || !t.endsWith("]")) return java.util.Collections.emptyList();
        t = t.substring(1, t.length()-1).trim();
        if (t.isEmpty()) return java.util.Collections.emptyList();
        String[] parts = t.split("\\|");
        java.util.ArrayList<String> list = new java.util.ArrayList<>(parts.length);
        for (String p : parts) {
            String v = p.trim();
            if (!v.isEmpty()) list.add(v);
        }
        return list;
    }


    private static float[] parseColor(String arg) {
        if (arg.startsWith("#")) {
            String hex = arg.substring(1);
            if (hex.length() == 6) {
                int v = Integer.parseInt(hex, 16);
                float r = ((v >> 16) & 0xFF) / 255f;
                float g = ((v >> 8)  & 0xFF) / 255f;
                float b = ( v        & 0xFF) / 255f;
                return new float[]{r, g, b};
            }
        }
        String[] parts = arg.split(",");
        if (parts.length == 3) {
            float r = Float.parseFloat(parts[0].trim());
            float g = Float.parseFloat(parts[1].trim());
            float b = Float.parseFloat(parts[2].trim());
            boolean scale255 = (r > 1f || g > 1f || b > 1f);
            if (scale255) { r /= 255f; g /= 255f; b /= 255f; }
            return new float[]{ clamp01(r), clamp01(g), clamp01(b) };
        }
        return new float[]{1f, 1f, 1f};
    }
}
