package eaglemixins.init;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("eaglemixins")
public final class ModItems {
    @ObjectHolder("tainted_tear")
    public static final Item TAINTED_TEAR = null; // injected after registration
    @ObjectHolder("void_tear")
    public static final Item VOID_TEAR = null;
    @ObjectHolder("enigmoth_eye")
    public static final Item ENIGMOTH_EYE = null;
    @ObjectHolder("ghostly_gills")
    public static final Item GHOSTLY_GILLS = null;
    private ModItems() {}
}
