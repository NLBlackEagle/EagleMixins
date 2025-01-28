package eaglemixins.mixin.fishsundeadrising;

import com.Fishmod.mod_LavaCow.entities.EntityAvaton;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityAvaton.AIUseSpell.class)
public class EntityAvatonAIUseSpellMixin {
    @Redirect(
            method = "castSpell",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z")
    )
    public boolean eagleMixins_furUndertakerAIUseSpell_castSpell(World instance, Entity entity){
        //Make them not drop loot or xp
        entity.getEntityData().setBoolean("xat:summoned",true);
        return instance.spawnEntity(entity);
    }
}
