package eaglemixins.handlers;

import biomesoplenty.common.block.BlockBOPDoor;
import eaglemixins.config.ForgeConfigHandler;
import nc.block.NCBlockDoor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DoorDupeHandler {

    @SubscribeEvent
    public static void onDoorBreak(BlockEvent.BreakEvent event) {
        if (!ForgeConfigHandler.ServerConfig.fixBOPDoorDupe) return;

        World world = (World) event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // Only Biomes O Plenty doors
        if (!((block instanceof BlockBOPDoor) || (block instanceof NCBlockDoor))) return;

        // Only act on lower part of door
        if (state.getValue(BlockDoor.HALF) != BlockDoor.EnumDoorHalf.LOWER) return;

        EntityPlayer player = event.getPlayer();

        BlockPos upper = pos.up();
        IBlockState upperState = world.getBlockState(upper);
        if (upperState.getBlock() == block) {
            // Fake player harvesting upper block
            upperState.getBlock().onBlockHarvested(world, upper, upperState, player);
            upperState.getBlock().breakBlock(world, upper, upperState);
            world.setBlockToAir(upper); // removes block & drops nothing
        }

        // Cancel original break event
        event.setCanceled(true);

        // Removes
        if (block instanceof NCBlockDoor) return;

        // Play break sound
        world.playSound(null, pos,
                block.getSoundType(state, world, pos, player).getBreakSound(),
                net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);

        // Play break particles
        world.playEvent(2001, pos, Block.getStateId(state));

        // Call vanilla cleanup
        block.onBlockHarvested(world, pos, state, player);
        block.breakBlock(world, pos, state);
        world.setBlockToAir(pos);

        // Spawn correct item manually if not creative
        if (!player.capabilities.isCreativeMode) {
            Item item = Item.getItemFromBlock(block);
            if (item != null) {
                Block.spawnAsEntity(world, pos, new ItemStack(item));
            }
        }
    }
}
