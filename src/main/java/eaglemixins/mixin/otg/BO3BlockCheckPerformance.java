package eaglemixins.mixin.otg;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.customobjects.bo3.BO3;
import com.pg85.otg.customobjects.bo3.BO3Config;
import com.pg85.otg.customobjects.structures.CustomStructure;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.materials.MaterialSet;
import com.pg85.otg.util.materials.MaterialSetEntry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Mixin(BO3.class)
public abstract class BO3BlockCheckPerformance {
    @Shadow(remap = false) private BO3Config settings;

    @Unique private final Set<Integer> eaglemixins$sourceStates = new HashSet<>();

    @Inject(
            method = "onEnable",
            at = @At(value = "FIELD", target = "Lcom/pg85/otg/customobjects/bo3/BO3Config;settingsMode:Lcom/pg85/otg/configuration/world/WorldConfig$ConfigMode;", ordinal = 0),
            remap = false
    )
    private void eaglemixins_parseSourceBlocks(CallbackInfoReturnable<Boolean> cir){
        for(MaterialSetEntry entry : ((BO3ConfigAccessor) settings).getSourceBlocks().materials) {
            eaglemixins$sourceStates.add(entry.material.getBlockId() * 16 + entry.material.getBlockData());
        }
    }

    @Inject(
            method = "trySpawnAt",
            at = @At("HEAD"),
            remap = false
    )
    private void eaglemixins_checkIfSourceBlocksContainsAir(CustomStructure structure, LocalWorld world, Random random, Rotation rotation, int x, int y, int z, int minY, int maxY, int baseY, ChunkCoordinate chunkBeingPopulated, boolean replaceBlocks,
            CallbackInfoReturnable<Boolean> cir,
            @Share("isBukkitWorld") LocalBooleanRef isBukkitWorld,
            @Share("pos")LocalRef<BlockPos.MutableBlockPos> pos
    ){
        isBukkitWorld.set(!(world instanceof ForgeWorld));
        if(isBukkitWorld.get()) return;
        pos.set(new BlockPos.MutableBlockPos());
    }

    @WrapOperation(
            method = "trySpawnAt",
            at = @At(value = "INVOKE", target = "Lcom/pg85/otg/common/LocalWorld;getMaterial(IIILcom/pg85/otg/util/ChunkCoordinate;)Lcom/pg85/otg/common/LocalMaterialData;"),
            remap = false
    )
    private LocalMaterialData eaglemixins_dontGetMaterial(
            LocalWorld instance, int x, int y, int z, ChunkCoordinate chunkBeingPopulated, Operation<LocalMaterialData> original,
            @Share("isBukkitWorld") LocalBooleanRef isBukkitWorld,
            @Share("x") LocalIntRef xRef,
            @Share("y") LocalIntRef yRef,
            @Share("z") LocalIntRef zRef
    ){
        if(isBukkitWorld.get()) return original.call(instance, x, y, z, chunkBeingPopulated);
        xRef.set(x);
        yRef.set(y);
        zRef.set(z);
        return null;
    }

    @WrapOperation(
            method = "trySpawnAt",
            at = @At(value = "INVOKE", target = "Lcom/pg85/otg/util/materials/MaterialSet;contains(Lcom/pg85/otg/common/LocalMaterialData;)Z"),
            remap = false
    )
    private boolean eaglemixins_dontCheckContains(MaterialSet instance, LocalMaterialData material, Operation<Boolean> original,
            CustomStructure structure, LocalWorld localWorld,
            @Share("isBukkitWorld") LocalBooleanRef isBukkitWorld,
            @Share("x") LocalIntRef x,
            @Share("y") LocalIntRef y,
            @Share("z") LocalIntRef z,
            @Share("pos")LocalRef<BlockPos.MutableBlockPos> pos
    ){
        if(isBukkitWorld.get()) return original.call(instance, material);

        IBlockState state = ((ForgeWorld) localWorld).world.getBlockState(pos.get().setPos(x.get(),y.get(),z.get()));
        int hash = Block.getIdFromBlock(state.getBlock()) * 16 + state.getBlock().getMetaFromState(state);

        return eaglemixins$sourceStates.contains(hash);
    }
}
