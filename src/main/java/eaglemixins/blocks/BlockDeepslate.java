package eaglemixins.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockDeepslate extends Block {
    public BlockDeepslate() {
        super(Material.ROCK);
        setHardness(3.0F);
        setResistance(6.0F);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        // IMPORTANT: do NOT call world.updateComparatorOutputLevel(...)
        // The base implementation just removes TEs etc; that’s fine.
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return false;
    }
}
