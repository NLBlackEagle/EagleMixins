package eaglemixins.radiation;

import nc.capability.radiation.source.IRadiationSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadLocalRandom;

/** NBT-backed IRadiationSource for ItemStacks, supports fixed or range-based levels. */
public class NbtStackRadiation implements IRadiationSource, ICapabilityProvider {
    /** Fixed/locked radiation value (per-item rads). */
    public static final String NBT_KEY = "ncRadiation";
    /** Optional range keys. If present and ncRadiation is absent, a value in [min,max] is rolled once and stored into ncRadiation. */
    public static final String NBT_MIN = "ncRadiationMin";
    public static final String NBT_MAX = "ncRadiationMax";
    /** Optional marker that we've finalized from a range. */
    public static final String NBT_FINAL = "ncRadiationFinalized";

    private final ItemStack stack;

    public NbtStackRadiation(ItemStack stack) {
        this.stack = stack;
    }

    // ---- Capability plumbing ----
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        if (capability != IRadiationSource.CAPABILITY_RADIATION_SOURCE) return false;
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && (tag.hasKey(NBT_KEY) || (tag.hasKey(NBT_MIN) && tag.hasKey(NBT_MAX)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        return hasCapability(capability, facing) ? (T) this : null;
    }

    // ---- IRadiationSource (minimal impl for items) ----
    private NBTTagCompound getOrCreateTag() {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }

    private double read() {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) return 0.0D;

        // If a fixed value is present, just return it.
        if (tag.hasKey(NBT_KEY)) return tag.getDouble(NBT_KEY);

        // Otherwise, if a range is present, roll once, store, and return.
        if (tag.hasKey(NBT_MIN) && tag.hasKey(NBT_MAX)) {
            double min = tag.getDouble(NBT_MIN);
            double max = tag.getDouble(NBT_MAX);
            if (max < min) { double t = min; min = max; max = t; } // swap if user inverted

            if (min < 0.0D) min = 0.0D;
            if (max < 0.0D) max = 0.0D;

            double value = (max > min)
                    ? (min + ThreadLocalRandom.current().nextDouble() * (max - min))
                    : min;

            NBTTagCompound out = getOrCreateTag();
            out.setDouble(NBT_KEY, value);
            out.setBoolean(NBT_FINAL, true);
            return value;
        }

        return 0.0D;
    }

    private void write(double v) {
        NBTTagCompound tag = getOrCreateTag();
        if (v < 0.0D) v = 0.0D;
        tag.setDouble(NBT_KEY, v);
        tag.setBoolean(NBT_FINAL, true);
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

    // We persist via the ItemStack's own NBT, so these remain no-ops.
    @Override
    public NBTTagCompound writeNBT(IRadiationSource instance, EnumFacing side, NBTTagCompound nbt) { return null; }

    @Override
    public void readNBT(IRadiationSource instance, EnumFacing side, NBTTagCompound nbt) {}

}