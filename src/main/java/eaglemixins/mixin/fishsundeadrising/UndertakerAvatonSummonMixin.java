package eaglemixins.mixin.fishsundeadrising;

import com.Fishmod.mod_LavaCow.entities.EntityAvaton;
import com.Fishmod.mod_LavaCow.entities.EntityUndertaker;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = {
        EntityUndertaker.AIUseSpell.class,
        EntityAvaton.AIUseSpell.class
})
public class UndertakerAvatonSummonMixin {
    @ModifyArg(
            method = "castSpell",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z")
    )
    public Entity eagleMixins_furAIUseSpell_castSpell(Entity entity){
        //Make them not drop loot or xp
        entity.getEntityData().setBoolean("xat:summoned",true);
        return entity;
    }
}
