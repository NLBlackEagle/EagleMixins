package eaglemixins.mixin.reskillable;

import codersafterdark.reskillable.base.LevelLockHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LevelLockHandler.class)
public abstract class MixinReskillableBlockOverride {

    /**
     * Overwrites Reskillable's interaction lock check to bypass for NuclearCraft blocks.
     */
    @Overwrite
    @SubscribeEvent
    public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockPos pos = event.getPos();
        IBlockState state = event.getWorld().getBlockState(pos);
        Block block = state.getBlock();

        if (block.getRegistryName() != null && (block.getRegistryName().getNamespace().equals("cookingforblockheads") || block.getRegistryName().getNamespace().equals("nuclearcraft"))) {
            // Allow interaction unconditionally
            return;
        }

        // Call original logic from Reskillable
        LevelLockHandler.enforce(event);
        if (!event.isCanceled()) {
            if (event.getItemStack().isEmpty()) {
                EntityPlayer player = event.getEntityPlayer();
                LevelLockHandler.genericEnforce(event, player, event.getHand().equals(EnumHand.MAIN_HAND) ? player.getHeldItemOffhand() : player.getHeldItemMainhand(), "reskillable.misc.locked.item");
                if (event.isCanceled()) return;
            }

            ItemStack stack = new ItemStack(block, 1, block.getMetaFromState(state));
            if (stack.isEmpty()) {
                stack = block.getItem(event.getWorld(), pos, state);
            }

            if (block.hasTileEntity(state)) {
                TileEntity te = event.getWorld().getTileEntity(pos);
                if (te != null && !te.isInvalid()) {
                    stack.setTagCompound(te.writeToNBT(new NBTTagCompound()));
                }
            }

            LevelLockHandler.genericEnforce(event, event.getEntityPlayer(), stack, "reskillable.misc.locked.block_use");
        }
    }
}