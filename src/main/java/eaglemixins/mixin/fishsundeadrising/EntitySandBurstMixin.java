package eaglemixins.mixin.fishsundeadrising;

import com.Fishmod.mod_LavaCow.entities.projectiles.EntitySandBurst;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntitySandBurst.class)
public class EntitySandBurstMixin {
    @ModifyArg(
            method = "onUpdate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z")
    )
    public Entity eagleMixins_furSandBurst_onUpdate(Entity entity){
        //Make summoned forsaken not drop loot or xp
        entity.getEntityData().setBoolean("xat:summoned",true);
        return entity;
    }
}
