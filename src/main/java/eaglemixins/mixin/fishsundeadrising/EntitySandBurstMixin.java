package eaglemixins.mixin.fishsundeadrising;

import com.Fishmod.mod_LavaCow.entities.projectiles.EntitySandBurst;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntitySandBurst.class)
public class EntitySandBurstMixin {
    @Redirect(
            method = "onUpdate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"),
            remap = false
    )
    public boolean eagleMixins_furUndertakerAIUseSpell_castSpell(World instance, Entity entity){
        //Make summoned forsaken not drop loot or xp
        entity.getEntityData().setBoolean("xat:summoned",true);
        return instance.spawnEntity(entity);
    }
}
