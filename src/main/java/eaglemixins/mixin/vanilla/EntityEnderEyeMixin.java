package eaglemixins.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eaglemixins.client.EnderEyeTrail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The stronghold position is only known on the server (ItemEnderEye -> moveTowards), so the horizontal distance to it
 * is synced to the client through the data manager, where the portal trail gets colored by it.
 */
@Mixin(EntityEnderEye.class)
public abstract class EntityEnderEyeMixin extends Entity {

    @Unique
    private static final DataParameter<Float> EAGLEMIXINS$STRONGHOLD_DISTANCE = EntityDataManager.createKey(EntityEnderEye.class, DataSerializers.FLOAT);

    public EntityEnderEyeMixin(World world) {
        super(world);
    }

    @Inject(method = "entityInit", at = @At("TAIL"))
    private void eaglemixins$registerDistance(CallbackInfo ci) {
        this.dataManager.register(EAGLEMIXINS$STRONGHOLD_DISTANCE, -1.0F);
    }

    @Inject(method = "moveTowards", at = @At("HEAD"))
    private void eaglemixins$storeDistance(BlockPos pos, CallbackInfo ci) {
        double dx = pos.getX() - this.posX;
        double dz = pos.getZ() - this.posZ;
        this.dataManager.set(EAGLEMIXINS$STRONGHOLD_DISTANCE, (float) Math.sqrt(dx * dx + dz * dz));
    }

    @WrapOperation(
            method = "onUpdate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V")
    )
    private void eaglemixins$coloredTrail(World world, EnumParticleTypes type, double x, double y, double z, double vx, double vy, double vz, int[] params, Operation<Void> original) {
        float distance = this.dataManager.get(EAGLEMIXINS$STRONGHOLD_DISTANCE);
        if (type == EnumParticleTypes.PORTAL && world.isRemote && distance >= 0.0F) {
            EnderEyeTrail.spawnPortalParticle(x, y, z, vx, vy, vz, distance);
            return;
        }
        original.call(world, type, x, y, z, vx, vy, vz, params);
    }
}
