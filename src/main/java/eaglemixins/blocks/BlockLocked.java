package eaglemixins.blocks;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.LockedBlockConfig;
import eaglemixins.handlers.LockVanishHandler;
import eaglemixins.init.ModSounds;
import eaglemixins.items.ItemLockKey;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Obsidian lock (keyblock.bbmodel) that vanishes when right-clicked with a key that has the same LockId, which is used up.
 * The LockId is stored in TileEntityLock, names and lore come from LockedBlockConfig.
 */
public class BlockLocked extends BlockLockFrame {

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityLock();
    }

    public static String getLockId(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return te instanceof TileEntityLock ? ((TileEntityLock) te).getLockId() : "";
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() instanceof ItemLockKey) {
            if (world.isRemote) return true;
            String lockId = getLockId(world, pos);
            String keyId = ItemLockKey.getLockId(held);
            if (lockId.isEmpty() && player.isCreative() && !keyId.isEmpty()) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof TileEntityLock) ((TileEntityLock) te).setLockId(keyId);
                player.sendStatusMessage(new TextComponentTranslation("eaglemixins.lock.bound", keyId), true);
            } else if (!lockId.isEmpty() && lockId.equals(keyId)) {
                open(world, pos, lockId, player, held);
            } else {
                player.sendStatusMessage(new TextComponentTranslation("eaglemixins.lock.wrong_key"), true);
            }
            return true;
        }
        // Let block items through so things can still be placed against locks
        if (hand != EnumHand.MAIN_HAND || held.getItem() instanceof ItemBlock) return false;
        if (!world.isRemote) sendLore(player, getLockId(world, pos));
        return true;
    }

    private static void sendLore(EntityPlayer player, String lockId) {
        LockedBlockConfig.LockType type = ForgeConfigHandler.lockedblocks.getLockType(lockId);
        if (type == null || type.lockLore.isEmpty()) {
            player.sendMessage(new TextComponentTranslation("eaglemixins.lock.locked"));
            return;
        }
        for (String line : type.lockLore) player.sendMessage(new TextComponentString(line));
    }

    /**
     * Opens this lock: it and every connected keyslave (plus connected locks with the same LockId if Open Connected Locks is on)
     * vanish one by one, nearest first, see LockVanishHandler.
     */
    private void open(World world, BlockPos origin, String lockId, EntityPlayer player, ItemStack held) {
        LockVanishHandler.schedule(world, findConnected(world, origin, lockId));
        world.playSound(null, origin, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 1.0F, 0.6F);
        player.sendMessage(new TextComponentTranslation("eaglemixins.lock.opened"));

        if (!(player.isCreative() && ForgeConfigHandler.lockedblocks.keepKeyInCreative)) held.shrink(1);
    }

    /** Blocks that open together with the lock at origin, in breadth-first order. */
    private List<BlockPos> findConnected(World world, BlockPos origin, String lockId) {
        boolean openConnected = ForgeConfigHandler.lockedblocks.openConnected;
        int max = ForgeConfigHandler.lockedblocks.maxConnected;
        List<BlockPos> found = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(origin);
        while (!queue.isEmpty() && found.size() < max) {
            BlockPos pos = queue.poll();
            Block block = world.getBlockState(pos).getBlock();
            boolean opens = block instanceof BlockLockSlave
                    || pos.equals(origin)
                    || (block == this && openConnected && lockId.equals(getLockId(world, pos)));
            if (!opens) continue;
            found.add(pos);
            for (EnumFacing side : EnumFacing.values()) {
                BlockPos next = pos.offset(side);
                if (visited.add(next)) queue.add(next);
            }
        }
        return found;
    }

    /** The block vanishes into smoke with a deepslate place sound. Server-side only, WorldServer sends the particles to nearby players. */
    public static void vanish(WorldServer world, BlockPos pos) {
        if (!(world.getBlockState(pos).getBlock() instanceof BlockLockFrame)) return; // replaced in the meantime
        double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;
        world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, y, z, 12, 0.3, 0.3, 0.3, 0.02);
        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 20, 0.4, 0.4, 0.4, 0.03);
        world.playSound(null, pos, ModSounds.DEEPSLATE_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F + world.rand.nextFloat() * 0.2F);
        world.setBlockToAir(pos);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return createStack(getLockId(world, pos));
    }

    public ItemStack createStack(String lockId) {
        ItemStack stack = new ItemStack(this);
        if (!lockId.isEmpty()) {
            NBTTagCompound blockEntityTag = new NBTTagCompound();
            blockEntityTag.setString(TileEntityLock.TAG, lockId);
            stack.setTagInfo("BlockEntityTag", blockEntityTag);
        }
        return stack;
    }
}
