package eaglemixins.mixin.vanilla;

import com.llamalad7.mixinextras.sugar.Local;
import eaglemixins.handlers.BerianHandler;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityZombie.class)
public abstract class ZombifyBerians extends EntityMob {
    public ZombifyBerians(World worldIn) {
        super(worldIn);
    }

    @Inject(
            method = "onKillEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z")
    )
    private void eagleMixins_vanillaEntityZombieVillager_finishConversion(CallbackInfo ci, @Local EntityZombieVillager zombieVill, @Local EntityVillager targetVill){
        BerianHandler.copyBerianTags(targetVill, zombieVill);
    }
}