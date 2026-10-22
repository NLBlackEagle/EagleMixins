package eaglemixins.blocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/** Stores which keys open a locked_block. Set from the item's BlockEntityTag when placed. */
public class TileEntityLock extends TileEntity {
    public static final String TAG = "LockId";

    private String lockId = "";

    public String getLockId() { return lockId; }

    public void setLockId(String lockId) {
        this.lockId = lockId;
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        lockId = compound.getString(TAG);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString(TAG, lockId);
        return compound;
    }

    // Synced on chunk load so pick block works on the client
    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }
}
