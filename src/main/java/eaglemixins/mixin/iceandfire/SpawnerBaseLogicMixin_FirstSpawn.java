package eaglemixins.mixin.iceandfire;

import com.github.alexthe666.iceandfire.entity.tile.SpawnerBaseLogic;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Ice and Fire spawners (iceandfire:monster_spawner, iceandfire:dread_spawner) put their boss in SpawnData.
 * When MaxNearbyEntities was reached, the spawner reset its timer, which rolls a new SpawnData from SpawnPotentials,
 * so the boss could be skipped for good. A Delay of -1 also rolled SpawnPotentials before anything spawned.
 * Until the first entity has spawned, the nearby count is ignored and SpawnData is kept.
 */
@Mixin(SpawnerBaseLogic.class)
public abstract class SpawnerBaseLogicMixin_FirstSpawn {

    @Unique
    private static final String eaglemixins$FIRST_SPAWN_DONE = "EagleMixinsFirstSpawnDone";

    @Unique
    private boolean eaglemixins$firstSpawnDone = false;

    /** Delay -1: don't reroll SpawnData before the first spawn, spawn right away instead */
    @WrapOperation(
            method = "updateSpawner",
            at = @At(value = "INVOKE", target = "Lcom/github/alexthe666/iceandfire/entity/tile/SpawnerBaseLogic;resetTimer()V", ordinal = 0, remap = false)
    )
    private void eaglemixins$keepFirstSpawnData(SpawnerBaseLogic logic, Operation<Void> original) {
        if (eaglemixins$firstSpawnDone) original.call(logic);
    }

    @ModifyExpressionValue(
            method = "updateSpawner",
            at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", remap = false)
    )
    private int eaglemixins$ignoreNearbyOnFirstSpawn(int nearby) {
        return eaglemixins$firstSpawnDone ? nearby : 0;
    }

    @Inject(
            method = "updateSpawner",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/storage/AnvilChunkLoader;spawnEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;)V", shift = At.Shift.AFTER)
    )
    private void eaglemixins$markFirstSpawn(CallbackInfo ci) {
        eaglemixins$firstSpawnDone = true;
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void eaglemixins$readFirstSpawn(NBTTagCompound compound, CallbackInfo ci) {
        eaglemixins$firstSpawnDone = compound.getBoolean(eaglemixins$FIRST_SPAWN_DONE);
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"))
    private void eaglemixins$writeFirstSpawn(NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> cir) {
        cir.getReturnValue().setBoolean(eaglemixins$FIRST_SPAWN_DONE, eaglemixins$firstSpawnDone);
    }
}
