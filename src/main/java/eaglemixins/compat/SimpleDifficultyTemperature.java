package eaglemixins.compat;

import com.charles445.simpledifficulty.api.temperature.TemperatureUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Only touch this class when Simple Difficulty is loaded */
public class SimpleDifficultyTemperature {

    /** Ambient Simple Difficulty temperature (0-25) at this position, or null if Simple Difficulty isn't ready */
    public static Integer getWorldTemperature(World world, BlockPos pos) {
        if (TemperatureUtil.internal == null) return null;
        return TemperatureUtil.getWorldTemperature(world, pos);
    }
}
