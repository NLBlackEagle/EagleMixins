package eaglemixins.handlers;

import eaglemixins.blocks.BlockLocked;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.nbt.NBTBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Makes the blocks of an opened lock vanish one by one, one every vanish delay of its lock type
 * (or the global "Vanish Delay"). Delays under a tick vanish several blocks per tick.
 * <p>
 * Pending blocks are stored per dimension (data/eaglemixins_lock_vanish.dat) and written to disk as soon as a lock is opened,
 * so a crash, logout or server stop resumes the door where it left off. Blocks in unloaded chunks wait until their chunk loads,
 * a sequence only finishes once every one of its blocks has been cleared.
 */
public class LockVanishHandler {

    /** First block vanishes right away, the rest one per delay. */
    public static void schedule(World world, List<BlockPos> blocks, String lockId) {
        if (!(world instanceof WorldServer) || blocks.isEmpty()) return;
        WorldServer server = (WorldServer) world;
        Sequence sequence = new Sequence(blocks, lockId, ForgeConfigHandler.lockedblocks.getVanishDelayTicks(lockId));
        sequence.progress = sequence.delay; // first block right away
        sequence.step(server);
        Data data = Data.get(server);
        if (!sequence.blocks.isEmpty()) data.sequences.add(sequence);
        data.markDirty();
        saveNow(server);
    }

    /**
     * Writes the pending locks and the player inventories (with the used-up key) to disk right away, in the same tick,
     * instead of waiting for the next autosave. A crash right after opening then neither loses the door nor gives the key back.
     */
    private static void saveNow(WorldServer world) {
        world.getPerWorldStorage().saveAllData();
        MinecraftServer server = world.getMinecraftServer();
        if (server == null) return;
        PlayerList players = server.getPlayerList();
        players.saveAllPlayerData(); // playerdata/<uuid>.dat
        // In singleplayer the host's inventory is loaded from level.dat, so that has to be written too
        NBTTagCompound host = players.getHostPlayerData();
        WorldServer overworld = server.getWorld(0);
        if (host != null && overworld != null) overworld.getSaveHandler().saveWorldInfoWithPlayer(overworld.getWorldInfo(), host);
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.world instanceof WorldServer)) return;
        WorldServer world = (WorldServer) event.world;
        Data data = Data.get(world);
        if (data.sequences.isEmpty()) return;
        Iterator<Sequence> it = data.sequences.iterator();
        while (it.hasNext()) {
            Sequence sequence = it.next();
            sequence.progress++;
            sequence.step(world);
            if (sequence.blocks.isEmpty()) it.remove();
        }
        data.markDirty();
    }

    private static class Sequence {
        final ArrayDeque<BlockPos> blocks;
        final String lockId; // picks the replacement blocks
        final double delay; // ticks per block, fixed when the lock is opened
        double progress = 0; // ticks waited towards the next block

        Sequence(List<BlockPos> blocks, String lockId, double delay) {
            this.blocks = new ArrayDeque<>(blocks);
            this.lockId = lockId;
            this.delay = delay;
        }

        /**
         * Vanishes every block whose delay has passed, all of them when the delay is 0.
         * Blocks that are already gone are dropped without using up any delay. A block in an unloaded chunk stops the sequence
         * until it loads again, keeping the nearest-first order.
         */
        void step(WorldServer world) {
            while (!blocks.isEmpty()) {
                BlockPos pos = blocks.peek();
                if (!world.isBlockLoaded(pos)) {
                    progress = Math.min(progress, delay); // don't pile up time while waiting
                    return;
                }
                if (!BlockLocked.isLockBlock(world, pos)) {
                    blocks.poll(); // already cleared (e.g. vanished before a crash and the chunk got saved)
                    continue;
                }
                if (delay > 0 && progress < delay) return;
                BlockLocked.vanish(world, pos, lockId);
                if (BlockLocked.isLockBlock(world, pos)) return; // didn't clear, retry next tick
                blocks.poll();
                progress -= delay;
            }
        }

        NBTTagCompound write() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("LockId", lockId);
            tag.setDouble("Delay", delay);
            tag.setDouble("Progress", progress);
            NBTTagList list = new NBTTagList();
            for (BlockPos pos : blocks) list.appendTag(new NBTTagLong(pos.toLong()));
            tag.setTag("Blocks", list);
            return tag;
        }

        static Sequence read(NBTTagCompound tag) {
            List<BlockPos> blocks = new ArrayList<>();
            for (NBTBase el : tag.getTagList("Blocks", Constants.NBT.TAG_LONG)) blocks.add(BlockPos.fromLong(((NBTTagLong) el).getLong()));
            Sequence sequence = new Sequence(blocks, tag.getString("LockId"), tag.getDouble("Delay"));
            sequence.progress = tag.getDouble("Progress");
            return sequence;
        }
    }

    /** Pending lock sequences of one dimension. */
    public static class Data extends WorldSavedData {
        private static final String DATA_NAME = "eaglemixins_lock_vanish";

        final List<Sequence> sequences = new ArrayList<>();

        // Used by MapStorage.getOrLoadData
        public Data(String name) {
            super(name);
        }

        static Data get(WorldServer world) {
            MapStorage storage = world.getPerWorldStorage();
            Data data = (Data) storage.getOrLoadData(Data.class, DATA_NAME);
            if (data == null) {
                data = new Data(DATA_NAME);
                storage.setData(DATA_NAME, data);
            }
            return data;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            sequences.clear();
            for (NBTBase el : nbt.getTagList("Sequences", Constants.NBT.TAG_COMPOUND)) {
                Sequence sequence = Sequence.read((NBTTagCompound) el);
                if (!sequence.blocks.isEmpty()) sequences.add(sequence);
            }
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            NBTTagList list = new NBTTagList();
            for (Sequence sequence : sequences) list.appendTag(sequence.write());
            nbt.setTag("Sequences", list);
            return nbt;
        }
    }
}
