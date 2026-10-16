package eaglemixins.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;

public class BlockDeepslateStairs extends BlockStairs {
    public BlockDeepslateStairs(Block base) {
        super(base.getDefaultState());
        setHardness(4.5F);
        setResistance(6.0F);
        setHarvestLevel("pickaxe", 1);
        useNeighborBrightness = true;
    }
}
