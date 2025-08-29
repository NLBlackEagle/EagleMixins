package eaglemixins.radiation;

import nc.capability.radiation.source.IRadiationSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;

/** NBT-backed IRadiationSource for ItemStacks. */
public class NbtStackRadiation implements IRadiationSource, ICapabilityProvider {
    public static final String NBT_KEY = "ncRadiation"; // per-item rads

    private final ItemStack stack;

    public NbtStackRadiation(ItemStack stack) {
        this.stack = stack;
    }

    // ---- Capability plumbing ----
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        if (capability != IRadiationSource.CAPABILITY_RADIATION_SOURCE) return false;
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey(NBT_KEY); // present even if 0.0
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        return hasCapability(capability, facing) ? (T) this : null;
    }

    // ---- IRadiationSource (minimal impl for items) ----
    private double read() {
        NBTTagCompound tag = stack.getTagCompound();
        return (tag != null && tag.hasKey(NBT_KEY)) ? tag.getDouble(NBT_KEY) : 0.0D;
    }
    private void write(double v) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) { tag = new NBTTagCompound(); stack.setTagCompound(tag); }
        tag.setDouble(NBT_KEY, v);
    }

    @Override public double getRadiationLevel() { return read(); }
    @Override public void setRadiationLevel(double level) { write(level); }

    // Items don’t use these; return 0 / no-op to satisfy the interface.
    @Override public double getRadiationBuffer() { return 0.0; }
    @Override public void setRadiationBuffer(double buffer) {}
    @Override public double getScrubbingFraction() { return 0.0; }
    @Override public void setScrubbingFraction(double fraction) {}
    @Override public double getEffectiveScrubberCount() { return 0.0; }
    @Override public void setEffectiveScrubberCount(double count) {}

    @Override
    public NBTTagCompound writeNBT(IRadiationSource iRadiationSource, EnumFacing enumFacing, NBTTagCompound nbtTagCompound) {
        return null;
    }

    @Override
    public void readNBT(IRadiationSource iRadiationSource, EnumFacing enumFacing, NBTTagCompound nbtTagCompound) {

    }
}
