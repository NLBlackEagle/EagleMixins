package eaglemixins.init;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("eaglemixins")
public final class ModItems {
    @ObjectHolder("tainted_tear")
    public static final Item TAINTED_TEAR = null; // injected after registration
    private ModItems() {}
}
