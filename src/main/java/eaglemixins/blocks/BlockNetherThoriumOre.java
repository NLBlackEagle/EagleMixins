package eaglemixins.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/** Thorium ore in netherrack. Drops itself, smelting and ore dictionary are set up in init. Only registered when NuclearCraft is loaded. */
public class BlockNetherThoriumOre extends Block {
    public BlockNetherThoriumOre() {
        super(Material.ROCK);
        setHardness(3.0F);
        setResistance(5.0F);
        setHarvestLevel("pickaxe", 2);
        setSoundType(SoundType.STONE);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }
}
