package eaglemixins.util;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * A number that is either fixed ("1.2") or rolled uniformly between two bounds ("0.9~1.4").
 */
public final class RandomRange {
    public final double min;
    public final double max;

    private RandomRange(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public static RandomRange of(double value) {
        return new RandomRange(value, value);
    }

    public static RandomRange of(double min, double max) {
        return new RandomRange(min, max);
    }

    /** Parses "1.2" or "0.9~1.4", or returns null on bad input. */
    @Nullable
    public static RandomRange parse(String input) {
        String[] parts = input.split("~");
        try {
            if (parts.length == 1) return of(Double.parseDouble(parts[0].trim()));
            if (parts.length == 2) return of(Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim()));
        } catch (NumberFormatException ignored) {}
        return null;
    }

    public double roll(Random rand) {
        return min == max ? min : min + rand.nextDouble() * (max - min);
    }

    @Override
    public String toString() {
        return min == max ? String.valueOf(min) : min + "~" + max;
    }
}
