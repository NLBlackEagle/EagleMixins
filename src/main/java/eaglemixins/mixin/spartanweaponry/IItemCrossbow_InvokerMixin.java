package eaglemixins.mixin.spartanweaponry;

import com.oblivioussp.spartanweaponry.item.ItemCrossbow;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemCrossbow.class)
public interface IItemCrossbow_InvokerMixin {

    @Invoker(value = "calculateEntityViewVector",remap = false)
    Vec3d invokeCalculateEntityViewVector(float pitch, float yaw);
}
