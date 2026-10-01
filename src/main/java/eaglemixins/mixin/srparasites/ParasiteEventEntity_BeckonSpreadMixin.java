package eaglemixins.mixin.srparasites;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPStationaryArchitect;
import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityParasiteBase;
import com.dhanantry.scapeandrunparasites.entity.monster.deterrent.nexus.EntityVenkrolSIV;
import com.dhanantry.scapeandrunparasites.util.ParasiteEventEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.handlers.SRParasitesHandler;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ParasiteEventEntity.class)
public abstract class ParasiteEventEntity_BeckonSpreadMixin {
    @WrapOperation(
            method = "SummonM(Lcom/dhanantry/scapeandrunparasites/entity/ai/misc/EntityParasiteBase;[Ljava/lang/String;IDDDLnet/minecraft/entity/EntityLivingBase;Z)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z", remap = true),
            remap = false
    )
    private static boolean eagleMixins_srpParasiteEventEntity_SummonM(World world, Entity entity, Operation<Boolean> original, @Local(argsOnly = true) EntityParasiteBase summoner) {
        //Stage IV Beckons spawn their Beckons further away, cancel the spawn if there is no valid spot
        if (summoner instanceof EntityVenkrolSIV && SRParasitesHandler.isBeckon(entity)
                && !SRParasitesHandler.relocateSpreadBeckon(world, summoner, entity))
            return false;
        return original.call(world, entity);
    }

    @WrapOperation(
            method = "SummonM(Lcom/dhanantry/scapeandrunparasites/entity/ai/misc/EntityParasiteBase;[Ljava/lang/String;IDDDLnet/minecraft/entity/EntityLivingBase;Z)Z",
            at = @At(value = "INVOKE", target = "Lcom/dhanantry/scapeandrunparasites/entity/ai/misc/EntityPStationaryArchitect;setCanGrowTo(Z)V"),
            remap = false
    )
    private static void eagleMixins_srpParasiteEventEntity_SummonM_canGrow(EntityPStationaryArchitect beckon, boolean canGrow, Operation<Void> original) {
        //SRP stops Beckons summoned by Stage IV Beckons from growing, optionally allow it
        if (!ForgeConfigHandler.srparasites.beckonSpreadCanGrow) original.call(beckon, canGrow);
    }
}
