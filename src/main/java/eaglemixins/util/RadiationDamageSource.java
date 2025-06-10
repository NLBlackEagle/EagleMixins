package eaglemixins.util;

import net.minecraft.util.DamageSource;

public class RadiationDamageSource {

    public static final DamageSource RADIATION = new DamageSource("radiation")
            .setDamageBypassesArmor()
            .setDamageIsAbsolute();
}

