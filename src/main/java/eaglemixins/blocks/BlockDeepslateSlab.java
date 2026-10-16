package eaglemixins.blocks;

import eaglemixins.init.ModSounds;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/** Deepslate slab. Use {@link HalfSlab} and {@link DoubleSlab}, isDouble() is called from the Block constructor so it can't be a field. */
public abstract class BlockDeepslateSlab extends BlockSlab {
    // ItemSlab needs a variant property to compare against, slabs here only have one variant
    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);

    protected BlockDeepslateSlab() {
        super(Material.ROCK);
        setHardness(4.5F);
        setResistance(6.0F);
        setHarvestLevel("pickaxe", 1);
        setSoundType(ModSounds.DEEPSLATE);

        IBlockState state = blockState.getBaseState().withProperty(VARIANT, Variant.DEFAULT);
        if (!isDouble()) state = state.withProperty(HALF, EnumBlockHalf.BOTTOM);
        setDefaultState(state);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return isDouble() ? new BlockStateContainer(this, VARIANT) : new BlockStateContainer(this, HALF, VARIANT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        if (!isDouble()) state = state.withProperty(HALF, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return !isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP ? 8 : 0;
    }

    @Override
    public String getTranslationKey(int meta) {
        return getTranslationKey();
    }

    @Override
    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }

    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return Variant.DEFAULT;
    }

    public static class HalfSlab extends BlockDeepslateSlab {
        public HalfSlab() {
            setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
            useNeighborBrightness = true;
        }

        @Override
        public boolean isDouble() {
            return false;
        }
    }

    public static class DoubleSlab extends BlockDeepslateSlab {
        private final HalfSlab half;

        public DoubleSlab(HalfSlab half) {
            this.half = half;
        }

        @Override
        public boolean isDouble() {
            return true;
        }

        @Override
        public Item getItemDropped(IBlockState state, Random rand, int fortune) {
            return Item.getItemFromBlock(half); // BlockSlab drops 2 when double
        }

        @Override
        public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
            return new ItemStack(half);
        }

        @Override
        public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
            return false; // the double slab has no item, silk touch would drop nothing
        }
    }

    public enum Variant implements IStringSerializable {
        DEFAULT;

        @Override
        public String getName() {
            return "normal";
        }
    }
}
