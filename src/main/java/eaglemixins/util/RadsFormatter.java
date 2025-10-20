package eaglemixins.util;

import net.minecraft.util.math.MathHelper;

public final class RadsFormatter {
    public static String formatRads(double rads, int precision) {
        int orderOfMagnitude = (int) Math.floor(Math.log10(Math.abs(rads))) + 1;
        int digitsToUse = MathHelper.clamp(precision - orderOfMagnitude, 0 , precision);
        //use format xx.xx for numbers above 1 and 0.xxxx for numbers below 1 (if n=4)
        return String.format(java.util.Locale.ROOT, "%." + digitsToUse + "f", rads);
    }
}
