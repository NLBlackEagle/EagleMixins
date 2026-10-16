package eaglemixins.init;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("eaglemixins")
public final class ModBlocks {
    @ObjectHolder("deepslate")
    public static final Block DEEPSLATE = null; // injected after registration
    @ObjectHolder("cobbled_deepslate")
    public static final Block COBBLED_DEEPSLATE = null;
    @ObjectHolder("nether_thorium_ore")
    public static final Block NETHER_THORIUM_ORE = null;
    private ModBlocks() {}
}