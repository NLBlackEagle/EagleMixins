package eaglemixins.util;

import eaglemixins.EagleMixins;

import java.util.Random;

/**
 * A number that is either fixed ("1.2") or rolled uniformly between two bounds ("0.9~1.4").
 */
public final class RandomRange {
    public final double min;
    public final double max;

    private RandomRange(double min, double max) {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    public static RandomRange of(double value) {
        return new RandomRange(value, value);
    }

    public static RandomRange of(double min, double max) {
        return new RandomRange(min, max);
    }

    /**
     * Parses "1.2" or "0.9~1.4". Logs a warning and returns {@code fallback} as a fixed value on bad input.
     */
    public static RandomRange parse(String input, double fallback) {
        if (input == null || input.trim().isEmpty()) return of(fallback);
        String[] parts = input.split("~");
        try {
            if (parts.length == 1) return of(Double.parseDouble(parts[0].trim()));
            if (parts.length == 2) return of(Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim()));
        } catch (NumberFormatException ignored) {}
        EagleMixins.LOGGER.warn("Invalid number or range \"{}\", expected e.g. \"1.2\" or \"0.9~1.4\". Using {}", input, fallback);
        return of(fallback);
    }

    public double roll(Random rand) {
        return min == max ? min : min + rand.nextDouble() * (max - min);
    }
}
