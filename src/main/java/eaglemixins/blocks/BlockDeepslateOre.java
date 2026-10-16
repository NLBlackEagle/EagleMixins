package eaglemixins.blocks;

import eaglemixins.init.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * Deepslate ore. Without a drop source it drops itself (smelted like the stone variant),
 * with one it drops whatever the source ore drops (fortune and xp included), e.g. diamonds from minecraft:diamond_ore.
 */
public class BlockDeepslateOre extends Block {
    @Nullable private final ResourceLocation dropSource;

    public BlockDeepslateOre(int harvestLevel, @Nullable String dropSource) {
        super(Material.ROCK);
        this.dropSource = dropSource == null ? null : new ResourceLocation(dropSource);
        setHardness(4.5F);
        setResistance(15.0F);
        setHarvestLevel("pickaxe", harvestLevel);
        setSoundType(ModSounds.DEEPSLATE);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Nullable
    private IBlockState getDropSourceState() {
        if (dropSource == null) return null;
        Block block = ForgeRegistries.BLOCKS.getValue(dropSource);
        return block == null ? null : block.getDefaultState();
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        IBlockState source = getDropSourceState();
        if (source == null) super.getDrops(drops, world, pos, state, fortune);
        else source.getBlock().getDrops(drops, world, pos, source, fortune);
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        IBlockState source = getDropSourceState();
        return source == null ? 0 : source.getBlock().getExpDrop(source, world, pos, fortune);
    }
}
