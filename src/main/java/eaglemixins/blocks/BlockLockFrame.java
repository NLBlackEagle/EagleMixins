package eaglemixins.blocks;

import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

/** Shared properties of lock blocks and keyslaves: obsidian-like, protected from mobs, explosions and pistons, never drops. */
public abstract class BlockLockFrame extends Block {

    protected BlockLockFrame() {
        super(Material.ROCK);
        float hardness = ForgeConfigHandler.lockedblocks.hardness;
        if (hardness < 0) setBlockUnbreakable();
        else setHardness(hardness);
        setResistance(6000000.0F);
        setHarvestLevel("pickaxe", 3);
        setSoundType(SoundType.STONE);
        setLightOpacity(255);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return false; // withers, dragons
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.BLOCK;
    }

    // The models have recessed frames, so neighbours must still render the faces behind them
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
