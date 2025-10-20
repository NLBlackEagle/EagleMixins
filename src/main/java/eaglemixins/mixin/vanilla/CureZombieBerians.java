package eaglemixins.mixin.vanilla;

import com.llamalad7.mixinextras.sugar.Local;
import eaglemixins.handlers.BerianHandler;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityZombieVillager.class)
public abstract class CureZombieBerians extends EntityMob {
    public CureZombieBerians(World worldIn) {
        super(worldIn);
    }

    @Inject(
            method = "finishConversion",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z")
    )
    private void eagleMixins_vanillaEntityZombieVillager_finishConversion(CallbackInfo ci, @Local EntityVillager curedVill){
        BerianHandler.copyBerianTags(this, curedVill);
    }
}
