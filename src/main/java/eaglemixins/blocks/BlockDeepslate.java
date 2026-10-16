package eaglemixins.blocks;

import eaglemixins.init.ModBlocks;
import eaglemixins.init.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/** Deepslate and its decorative variants (cobbled, polished, bricks, tiles, ...). */
public class BlockDeepslate extends Block {
    public BlockDeepslate(float hardness) {
        super(Material.ROCK);
        setHardness(hardness);
        setResistance(6.0F);
        setHarvestLevel("pickaxe", 1);
        setSoundType(ModSounds.DEEPSLATE);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        // Deepslate drops cobbled deepslate like stone drops cobblestone (silk touch still drops deepslate)
        if (this == ModBlocks.DEEPSLATE) return Item.getItemFromBlock(ModBlocks.COBBLED_DEEPSLATE);
        return super.getItemDropped(state, rand, fortune);
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
