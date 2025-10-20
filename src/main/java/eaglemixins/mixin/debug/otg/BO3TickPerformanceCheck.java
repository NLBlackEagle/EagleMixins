package eaglemixins.mixin.debug.otg;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.generator.resource.TreeGen;
import com.pg85.otg.util.ChunkCoordinate;
import eaglemixins.debug.BO3_ChunkGen_Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Random;

@Mixin(TreeGen.class)
public abstract class BO3TickPerformanceCheck {

    @WrapOperation(
            method = "spawnInChunk",
            at = @At(value = "INVOKE", target = "Lcom/pg85/otg/customobjects/CustomObject;spawnAsTree(Lcom/pg85/otg/common/LocalWorld;Ljava/util/Random;IIIILcom/pg85/otg/util/ChunkCoordinate;)Z"),
            remap = false
    )
    private boolean eaglemixins_stopTime(CustomObject instance, LocalWorld world, Random random, int x, int z, int minY, int maxY, ChunkCoordinate chunkBeingPopulated, Operation<Boolean> original){
        if(!(instance instanceof BO3)) return original.call(instance, world, random, x, z, minY, maxY, chunkBeingPopulated);

        long startTime = System.currentTimeMillis();
        boolean returnValue = original.call(instance, world, random, x, z, minY, maxY, chunkBeingPopulated);
        int dur = (int) (System.currentTimeMillis() - startTime);

        if(returnValue) BO3_ChunkGen_Debug.ticksPerStructSuccess.put(instance.getName(), dur);
        else BO3_ChunkGen_Debug.ticksPerStructFail.put(instance.getName(), dur);

        return returnValue;
    }
}
