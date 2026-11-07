package eaglemixins.mixin.lycanitesmobs;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.creature.EntityVespid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Vespids only build a hive while following a Vespid Queen. Mob events (The Swarm) spawn queens,
 * so event mobs would leave permanent hives behind. Treat hiveBuilding as false for event vespids
 * or vespids whose queen came from an event.
 */
@Mixin(EntityVespid.class)
public abstract class EntityVespidMixin {
    @WrapOperation(
            method = "onLivingUpdate",
            at = @At(value = "FIELD", target = "Lcom/lycanitesmobs/core/entity/creature/EntityVespid;hiveBuilding:Z", remap = false)
    )
    private boolean eagleMixins$noEventHiveBuilding(EntityVespid vespid, Operation<Boolean> original) {
        if (!original.call(vespid)) return false;
        if (eagleMixins$isEventSpawned(vespid)) return false;
        return !(vespid.getMasterTarget() instanceof BaseCreatureEntity)
                || !eagleMixins$isEventSpawned((BaseCreatureEntity) vespid.getMasterTarget());
    }

    private static boolean eagleMixins$isEventSpawned(BaseCreatureEntity entity) {
        return entity.spawnEventType != null && !entity.spawnEventType.isEmpty();
    }
}
