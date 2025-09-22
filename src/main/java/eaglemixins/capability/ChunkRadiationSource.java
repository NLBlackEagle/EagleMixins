package eaglemixins.capability;

import eaglemixins.EagleMixins;
import nc.capability.radiation.source.IRadiationSource;
import nc.capability.radiation.source.RadiationSource;
import net.minecraft.nbt.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.BiConsumer;

public class ChunkRadiationSource extends RadiationSource {
    private final float[] subchunkRadiationLevel = new float[16];
    private final float[] subchunkRadiationBuffer = new float[16];
    private final float[] subchunkScrubbingFraction = new float[16];
    private final float[] subchunkEffectiveScrubberCount = new float[16];

    public ChunkRadiationSource(double startRadiation) {
        super(startRadiation);
        Arrays.fill(subchunkRadiationLevel, (float) startRadiation);
        Arrays.fill(subchunkRadiationBuffer, 0);
        Arrays.fill(subchunkScrubbingFraction, 0);
        Arrays.fill(subchunkEffectiveScrubberCount, 0);
    }

    private static final int NOT_SET = -1;
    private int currentSubchunk = NOT_SET;
    public void resetSubchunk(){
        this.currentSubchunk = NOT_SET;
    }
    public void setSubchunk(int idx){
        this.currentSubchunk = idx;
    }
    public void setSubchunk(BlockPos pos){
        this.currentSubchunk = MathHelper.clamp(pos.getY() >> 4, 0, 15);
    }
    public boolean subchunkIsReset(){
        return this.currentSubchunk == NOT_SET;
    }

    public float getSubchunkRadiationLevel(int subChunk) {
        return subchunkRadiationLevel[subChunk];
    }

    public float getSubchunkRadiationBuffer(int subChunk) {
        return subchunkRadiationBuffer[subChunk];
    }

    public float getSubchunkScrubbingFraction(int subChunk) {
        return subchunkScrubbingFraction[subChunk];
    }

    public float getSubchunkEffectiveScrubberCount(int subChunk) {
        return subchunkEffectiveScrubberCount[subChunk];
    }

    public void setSubchunkRadiationLevel(int subChunk, float subchunkRadiation) {
        this.subchunkRadiationLevel[subChunk] = subchunkRadiation;
    }

    public void setSubchunkRadiationBuffer(int subChunk, float subchunkRadiationBuffer) {
        this.subchunkRadiationBuffer[subChunk] = subchunkRadiationBuffer;
    }

    public void setSubchunkScrubbingFraction(int subChunk, float subchunkScrubbingFraction) {
        this.subchunkScrubbingFraction[subChunk] = subchunkScrubbingFraction;
    }

    public void setSubchunkEffectiveScrubberCount(int subChunk, float subchunkScrubberCount) {
        this.subchunkEffectiveScrubberCount[subChunk] = subchunkScrubberCount;
    }

    @Override
    public double getRadiationLevel() {
        if(this.currentSubchunk != NOT_SET)
            return this.getSubchunkRadiationLevel(currentSubchunk);
        if(!isDuringReadWrite) EagleMixins.LOGGER.error("EagleMixins getting radiation level from chunk radiation source without setting subchunk index! Notfiy nischhelm!");
        return (float) super.getRadiationLevel();
    }

    @Override
    public double getRadiationBuffer() {
        if(this.currentSubchunk != NOT_SET)
            return this.getSubchunkRadiationBuffer(currentSubchunk);
        if(!isDuringReadWrite) EagleMixins.LOGGER.error("EagleMixins getting radiation buffer from chunk radiation source without setting subchunk index! Notfiy nischhelm!");
        return super.getRadiationBuffer();
    }

    @Override
    public double getScrubbingFraction() {
        if(this.currentSubchunk != NOT_SET)
            return this.getSubchunkScrubbingFraction(currentSubchunk);
        if(!isDuringReadWrite) EagleMixins.LOGGER.error("EagleMixins getting scrubbing fraction from chunk radiation source without setting subchunk index! Notfiy nischhelm!");
        return super.getScrubbingFraction();
    }

    @Override
    public double getEffectiveScrubberCount() {
        if(this.currentSubchunk != NOT_SET)
            return this.getSubchunkEffectiveScrubberCount(currentSubchunk);
        if(!isDuringReadWrite) EagleMixins.LOGGER.error("EagleMixins getting scrubber coount from chunk radiation source without setting subchunk index! Notfiy nischhelm!");
        return super.getEffectiveScrubberCount();
    }

    @Override
    public void setRadiationLevel(double newRads) {
        if (!isDuringReadWrite) {
            if(currentSubchunk != NOT_SET){
                this.setSubchunkRadiationLevel(this.currentSubchunk, (float) newRads);
                return;
            }
            Arrays.fill(this.subchunkRadiationLevel, (float) Math.max(newRads, 0));
            EagleMixins.LOGGER.warn("EagleMixins: Writing radiation to the whole chunk! This shouldn't happen as all methods should write to subchunk instead");
        }
        super.setRadiationLevel(newRads);
    }

    @Override
    public void setRadiationBuffer(double newBuffer) {
        if (!isDuringReadWrite) {
            if(currentSubchunk != NOT_SET){
                this.setSubchunkRadiationBuffer(this.currentSubchunk, (float) newBuffer);
                return;
            }
            Arrays.fill(this.subchunkRadiationBuffer, (float) Math.max(newBuffer, 0));
            EagleMixins.LOGGER.warn("EagleMixins: Writing radiation buffer to the whole chunk! This shouldn't happen as all methods should write to subchunk instead");
        }
        super.setRadiationLevel(newBuffer);
    }

    @Override
    public void setScrubbingFraction(double newFraction) {
        if (!isDuringReadWrite) {
            if(currentSubchunk != NOT_SET){
                this.setSubchunkScrubbingFraction(this.currentSubchunk, (float) newFraction);
                return;
            }
            Arrays.fill(this.subchunkScrubbingFraction, (float) MathHelper.clamp(newFraction, 0, 1));
            EagleMixins.LOGGER.warn("EagleMixins: Writing scrubbing fraction to the whole chunk! This shouldn't happen as all methods should write to subchunk instead");
        }
        super.setScrubbingFraction(newFraction);
    }

    @Override
    public void setEffectiveScrubberCount(double newScrubberCount) {
        if (!isDuringReadWrite) {
            if(currentSubchunk != NOT_SET){
                this.setSubchunkEffectiveScrubberCount(this.currentSubchunk, (float) newScrubberCount);
                return;
            }
            Arrays.fill(this.subchunkEffectiveScrubberCount, (float) Math.max(0, newScrubberCount));
            EagleMixins.LOGGER.warn("EagleMixins: Writing scrubber count to the whole chunk! This shouldn't happen as all methods should write to subchunk instead");
        }
        super.setEffectiveScrubberCount(newScrubberCount);
    }

    @Override
    public NBTTagCompound writeNBT(IRadiationSource instance, EnumFacing side, NBTTagCompound nbt) {
        nbt.setTag("subchunkRadiationLevel", writeListToNBT(this.subchunkRadiationLevel));
        nbt.setTag("subchunkRadiationBuffer", writeListToNBT(this.subchunkRadiationBuffer));
        nbt.setTag("subchunkScrubbingFraction", writeListToNBT(this.subchunkScrubbingFraction));
        nbt.setTag("subchunkEffectiveScrubberCount", writeListToNBT(this.subchunkEffectiveScrubberCount));

        isDuringReadWrite = true;
        super.writeNBT(instance, side, nbt);
        isDuringReadWrite = false;

        return nbt;
    }

    private static NBTTagByteArray writeListToNBT(float[] values){
        byte[] data = new byte[values.length * 4];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        for (float value : values) buffer.putFloat(value);
        return new NBTTagByteArray(data);
    }

    private boolean isDuringReadWrite = false;

    @Override
    public void readNBT(IRadiationSource instance, EnumFacing side, NBTTagCompound nbt) {
        readFromList(nbt, "subchunkRadiationLevel", this.subchunkRadiationLevel);
        readFromList(nbt, "subchunkRadiationBuffer", this.subchunkRadiationBuffer);
        readFromList(nbt, "subchunkScrubbingFraction", this.subchunkScrubbingFraction);
        readFromList(nbt, "subchunkEffectiveScrubberCount", this.subchunkEffectiveScrubberCount);

        isDuringReadWrite = true;
        super.readNBT(instance, side, nbt);
        isDuringReadWrite = false;
    }

    private static void readFromList(NBTTagCompound nbt, String key, float[] setter){
        if(!nbt.hasKey(key, Constants.NBT.TAG_BYTE_ARRAY)) return;
        ByteBuffer buffer = ByteBuffer.wrap(nbt.getByteArray(key));
        for(int idx = 0; idx < 16; idx++) setter[idx] = buffer.getFloat();
    }
}
