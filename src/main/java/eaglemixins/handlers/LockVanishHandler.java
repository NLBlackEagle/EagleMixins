package eaglemixins.handlers;

import eaglemixins.blocks.BlockLocked;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Makes the blocks of an opened lock vanish one by one, one every vanish delay of its lock type
 * (or the global "Vanish Delay"). Delays under a tick vanish several blocks per tick.
 * Pending blocks are not saved: if the server stops halfway, the rest of the door stays.
 */
public class LockVanishHandler {

    private static final List<Sequence> SEQUENCES = new ArrayList<>();

    private static class Sequence {
        final WorldServer world;
        final ArrayDeque<BlockPos> blocks;
        final double delay; // ticks per block, fixed when the lock is opened
        double progress = 0; // ticks waited towards the next block

        Sequence(WorldServer world, List<BlockPos> blocks, double delay) {
            this.world = world;
            this.blocks = new ArrayDeque<>(blocks);
            this.delay = delay;
        }
    }

    /** First block vanishes right away, the rest one per delay. */
    public static void schedule(World world, List<BlockPos> blocks, String lockId) {
        if (!(world instanceof WorldServer) || blocks.isEmpty()) return;
        Sequence sequence = new Sequence((WorldServer) world, blocks, ForgeConfigHandler.lockedblocks.getVanishDelayTicks(lockId));
        BlockLocked.vanish(sequence.world, sequence.blocks.poll());
        step(sequence);
        if (!sequence.blocks.isEmpty()) SEQUENCES.add(sequence);
    }

    /** Vanishes every block whose delay has passed, all of them when the delay is 0. */
    private static void step(Sequence sequence) {
        while (!sequence.blocks.isEmpty() && (sequence.delay <= 0 || sequence.progress >= sequence.delay)) {
            BlockLocked.vanish(sequence.world, sequence.blocks.poll());
            sequence.progress -= sequence.delay;
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote || SEQUENCES.isEmpty()) return;
        Iterator<Sequence> it = SEQUENCES.iterator();
        while (it.hasNext()) {
            Sequence sequence = it.next();
            if (sequence.world != event.world) continue;
            sequence.progress++;
            step(sequence);
            if (sequence.blocks.isEmpty()) it.remove();
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        SEQUENCES.removeIf(sequence -> sequence.world == event.getWorld());
    }
}
