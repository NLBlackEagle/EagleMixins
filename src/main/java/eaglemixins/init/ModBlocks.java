package eaglemixins.init;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("eaglemixins")
public final class ModBlocks {
    @ObjectHolder("deepslate")
    public static final Block DEEPSLATE = null; // injected after registration
    private ModBlocks() {}
}