package eaglemixins.handlers;

import com.charles445.simpledifficulty.api.SDFluids;
import com.charles445.simpledifficulty.api.item.IItemCanteen;
import com.charles445.simpledifficulty.api.thirst.ThirstEnum;
import knightminer.inspirations.library.InspirationsRegistry;
import knightminer.inspirations.library.recipe.cauldron.ICauldronRecipe.CauldronState;
import knightminer.inspirations.recipes.tileentity.TileCauldron;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Sneak + right click an Inspirations cauldron with a SimpleDifficulty canteen to pour one dose into it.
 * Purified doses become purified water, every other dose becomes water. A dose only goes into an empty cauldron
 * or one holding the same fluid, so purified water is never mixed into (and lost as) normal water.
 * A normal right click still takes a dose out (SimpleDifficulty's cauldron recipe).
 * Vanilla skips the block's own right click while sneaking with an item, which is why this is an event and not a cauldron recipe.
 */
public class CanteenCauldronHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!player.isSneaking()) return;

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof IItemCanteen)) return;
        IItemCanteen canteen = (IItemCanteen) stack.getItem();
        if (canteen.isCanteenEmpty(stack)) return;

        World world = event.getWorld();
        BlockPos pos = event.getPos();
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileCauldron)) return;
        TileCauldron cauldron = (TileCauldron) te;

        Fluid fluid = canteen.getThirstEnum(stack) == ThirstEnum.PURIFIED ? SDFluids.purifiedWater : FluidRegistry.WATER;
        if (fluid == null) return;

        int level = cauldron.getFluidLevel();
        if (level >= InspirationsRegistry.getCauldronMax()) return;
        if (level > 0 && cauldron.getState().getFluid() != fluid) return; // different contents, don't mix

        if (!world.isRemote) {
            cauldron.setState(CauldronState.fluid(fluid), true);
            cauldron.setFluidLevel(level + 1);
            if (!player.capabilities.isCreativeMode) canteen.removeDose(stack);
            world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 0.6F, 1.2F);
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }
}
