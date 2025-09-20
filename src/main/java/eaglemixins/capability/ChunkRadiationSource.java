package eaglemixins.capability;

import eaglemixins.EagleMixins;
import nc.capability.radiation.source.IRadiationSource;
import nc.capability.radiation.source.RadiationSource;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.function.BiConsumer;

public class ChunkRadiationSource extends RadiationSource {
    private final double[] subchunkRadiationLevel = new double[16];
    private final double[] subchunkRadiationBuffer = new double[16];
    private final double[] subchunkScrubbingFraction = new double[16];
    private final double[] subchunkEffectiveScrubberCount = new double[16];

    public ChunkRadiationSource(double startRadiation) {
        super(startRadiation);
        Arrays.fill(subchunkRadiationLevel, 0);
        Arrays.fill(subchunkRadiationBuffer, 0);
        Arrays.fill(subchunkScrubbingFraction, 0);
        Arrays.fill(subchunkEffectiveScrubberCount, 0);
    }

    private static final int NOT_SET = -1;
    private static final int ALL = 16;
    private int currentSubchunk = NOT_SET;
    public void resetSubchunk(){
        this.currentSubchunk = NOT_SET;
    };
    public int getSubchunk(){ return this.currentSubchunk; }
    public void setSubchunk(int idx){
        this.currentSubchunk = idx;
    }
    public void setSubchunk(BlockPos pos){
        this.currentSubchunk = MathHelper.clamp(pos.getY() >> 4, 0, 15);
    }
    public void setAffectsAllSubchunks(){
        this.currentSubchunk = ALL;
    }
    public boolean subchunkIsReset(){
        return this.currentSubchunk == NOT_SET;
    }

    public double getSubchunkRadiationLevel(int subChunk) {
        return subchunkRadiationLevel[subChunk];
    }

    public double getSubchunkRadiationBuffer(int subChunk) {
        return subchunkRadiationBuffer[subChunk];
    }

    public double getSubchunkScrubbingFraction(int subChunk) {
        return subchunkScrubbingFraction[subChunk];
    }

    public double getSubchunkEffectiveScrubberCount(int subChunk) {
        return subchunkEffectiveScrubberCount[subChunk];
    }

    public void setSubchunkRadiationLevel(int subChunk, double subchunkRadiation) {
        this.subchunkRadiationLevel[subChunk] = subchunkRadiation;
    }

    public void setSubchunkRadiationBuffer(int subChunk, double subchunkRadiationBuffer) {
        this.subchunkRadiationBuffer[subChunk] = subchunkRadiationBuffer;
    }

    public void setSubchunkScrubbingFraction(int subChunk, double subchunkScrubbingFraction) {
        this.subchunkScrubbingFraction[subChunk] = subchunkScrubbingFraction;
    }

    public void setSubchunkEffectiveScrubberCount(int subChunk, double subchunkScrubberCount) {
        this.subchunkEffectiveScrubberCount[subChunk] = subchunkScrubberCount;
    }

    @Override
    public double getRadiationLevel() {
        if(this.currentSubchunk != NOT_SET)
            return this.getSubchunkRadiationLevel(currentSubchunk);
        if(!isDuringReadWrite) EagleMixins.LOGGER.error("EagleMixins getting radiation level from chunk radiation source without setting subchunk index! Notfiy nischhelm!");
        return super.getRadiationLevel();
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
            if(currentSubchunk != NOT_SET && currentSubchunk != ALL){
                this.setSubchunkRadiationLevel(this.currentSubchunk, newRads);
                return;
            }
            Arrays.fill(this.subchunkRadiationLevel, Math.max(newRads, 0));
            if(currentSubchunk != ALL) {
                EagleMixins.LOGGER.warn("EagleMixins: Writing radiation to the whole chunk! This shouldn't happen as all methods should write to subchunk instead");
                try {
                    throw new Exception("breh");
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        }
        super.setRadiationLevel(newRads);
    }

    @Override
    public void setRadiationBuffer(double newBuffer) {
        if (!isDuringReadWrite) {
            if(currentSubchunk != NOT_SET && currentSubchunk != ALL){
                this.setSubchunkRadiationBuffer(this.currentSubchunk, newBuffer);
                return;
            }
            Arrays.fill(this.subchunkRadiationBuffer, Math.max(newBuffer, 0));
            if(currentSubchunk != ALL)
                EagleMixins.LOGGER.warn("EagleMixins: Writing radiation buffer to the whole chunk! This shouldn't happen as all methods should write to subchunk instead");
        }
        super.setRadiationLevel(newBuffer);
    }

    @Override
    public void setScrubbingFraction(double newFraction) {
        if (!isDuringReadWrite) {
            if(currentSubchunk != NOT_SET && currentSubchunk != ALL){
                this.setSubchunkScrubbingFraction(this.currentSubchunk, newFraction);
                return;
            }
            Arrays.fill(this.subchunkScrubbingFraction, MathHelper.clamp(newFraction, 0, 1));
            if(currentSubchunk != ALL)
                EagleMixins.LOGGER.warn("EagleMixins: Writing scrubbing fraction to the whole chunk! This shouldn't happen as all methods should write to subchunk instead");
        }
        super.setScrubbingFraction(newFraction);
    }

    @Override
    public void setEffectiveScrubberCount(double newScrubberCount) {
        if (!isDuringReadWrite) {
            if(currentSubchunk != NOT_SET && currentSubchunk != ALL){
                this.setSubchunkEffectiveScrubberCount(this.currentSubchunk, newScrubberCount);
                return;
            }
            Arrays.fill(this.subchunkEffectiveScrubberCount, Math.max(0, newScrubberCount));
            if(currentSubchunk != ALL)
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

    private static NBTTagList writeListToNBT(double[] values){
        NBTTagList list = new NBTTagList();
        for(double value : values) list.appendTag(new NBTTagDouble(value));
        return list;
    }

    private boolean isDuringReadWrite = false;

    @Override
    public void readNBT(IRadiationSource instance, EnumFacing side, NBTTagCompound nbt) {
        readFromList(nbt, "subchunkRadiationLevel", this::setSubchunkRadiationLevel);
        readFromList(nbt, "subchunkRadiationBuffer", this::setSubchunkRadiationBuffer);
        readFromList(nbt, "subchunkScrubbingFraction", this::setSubchunkScrubbingFraction);
        readFromList(nbt, "subchunkEffectiveScrubberCount", this::setSubchunkEffectiveScrubberCount);

        isDuringReadWrite = true;//!nbt.hasKey("subchunkRadiationLevel"); //compat for old chunks without subchunk data -> fill subchunk arrays
        super.readNBT(instance, side, nbt);
        isDuringReadWrite = false;
    }

    private static void readFromList(NBTTagCompound nbt, String key, BiConsumer<Integer, Double> setter){
        if(!nbt.hasKey(key)) return;
        NBTTagList list = nbt.getTagList(key, 6);
        int idx = 0;
        for(NBTBase tag : list) setter.accept(idx++, ((NBTTagDouble) tag).getDouble());
    }
}
