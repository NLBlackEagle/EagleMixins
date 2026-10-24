package eaglemixins.mixin.vanilla.spawner;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eaglemixins.entity.EntityPlayerBoss;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.WeightedSpawnerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/**
 * Spawners count nearby entities of the same class for MaxNearbyEntities, and every player boss is the same class.
 * With several boss spawners in one room, the first bosses filled up the others' count, which made those spawners
 * skip their boss and roll a SpawnPotentials entry instead, so the boss never spawned. Only same BossId bosses count now.
 */
@Mixin(MobSpawnerBaseLogic.class)
public abstract class MobSpawnerBaseLogicMixin_PlayerBoss {

    @Shadow
    private WeightedSpawnerEntity spawnData;

    @ModifyExpressionValue(
            method = "updateSpawner",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntitiesWithinAABB(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;")
    )
    private List<? extends Entity> eaglemixins$countSameBossOnly(List<? extends Entity> nearby) {
        String bossId = spawnData.getNbt().getString("BossId");
        if (bossId.isEmpty()) return nearby;
        List<Entity> sameBoss = new ArrayList<>();
        for (Entity entity : nearby) {
            if (!(entity instanceof EntityPlayerBoss) || bossId.equals(((EntityPlayerBoss) entity).getBossId())) sameBoss.add(entity);
        }
        return sameBoss;
    }
}
