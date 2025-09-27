package eaglemixins.handlers;

import com.google.common.collect.Lists;
import net.blay09.mods.cookingforblockheads.tile.TileCounter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TileCounterHandler  {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getUseBlock() == Event.Result.DENY || event.getWorld().isRemote) {
            return;
        }
        EntityPlayer player = event.getEntityPlayer();
        if (player.isSpectator()) {
            return;
        }
        BlockPos pos = event.getPos();
        World world = event.getWorld();
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileCounter) {
            fillCounterWithLoot(world, (TileCounter) tile, player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }
        EntityPlayer player = event.getPlayer();
        if (player.isSpectator()) {
            return;
        }
        BlockPos pos = event.getPos();
        World world = event.getWorld();
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileCounter) {
            fillCounterWithLoot(world, (TileCounter) tile, player);
        }
    }



    private static void fillCounterWithLoot(World world, TileCounter counter, EntityPlayer player) {
        ResourceLocation lootTable = ((ILootContainer) counter).getLootTable();
        if (lootTable != null) {
            LootTable loottable = world.getLootTableManager().getLootTableFromLocation(lootTable);
            Random random = new Random();
            LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer) world);
            if (player != null) {
                lootcontext$builder.withLuck(player.getLuck()).withPlayer(player);
            }
            List<ItemStack> stacks = loottable.generateLootForPools(random, lootcontext$builder.build());
            List<Integer> emptySlots = getEmptySlotsRandomized(counter, random);
            for (ItemStack stack : stacks) {
                if (emptySlots.isEmpty()) {
                    break;
                } else if (stack.isEmpty()) {
                    counter.getItemHandler().insertItem(emptySlots.remove(emptySlots.size() - 1), ItemStack.EMPTY, false);
                } else {
                    counter.getItemHandler().insertItem(emptySlots.remove(emptySlots.size() - 1), stack, false);
                }
            }
            ((eaglemixins.util.LootTableSetter) counter).eaglemixins$setLootTable(null);
            counter.markDirty();
            BlockPos p = counter.getPos();
            world.notifyBlockUpdate(p, world.getBlockState(p), world.getBlockState(p), 3);
        }
    }

    private static List<Integer> getEmptySlotsRandomized(TileCounter counter, Random rand) {
        List<Integer> list = Lists.newArrayList();
        for (int i = 0; i < counter.getItemHandler().getSlots(); ++i) {
            if (counter.getItemHandler().getStackInSlot(i).isEmpty()) {
                list.add(i);
            }
        }
        Collections.shuffle(list, rand);
        return list;
    }
}
