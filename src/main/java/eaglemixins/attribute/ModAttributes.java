package eaglemixins.attribute;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;

public class ModAttributes {

    public static final IAttribute RADIATION_RESISTANCE =
            (new RangedAttribute(null, "eaglemixins.radiationResistance", 0.0D, 0.0D, 1.0E12D))
                    .setDescription("Radiation Resistance")
                    .setShouldWatch(true);

}
