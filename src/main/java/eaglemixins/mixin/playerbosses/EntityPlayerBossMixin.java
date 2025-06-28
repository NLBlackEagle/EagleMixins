package eaglemixins.mixin.playerbosses;

import com.lothrazar.playerbosses.EntityPlayerBoss;
import eaglemixins.handlers.util.AbyssalRiftSpawn;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerBoss.class)
@Implements({
        @Interface(iface = AbyssalRiftSpawn.class, prefix = "abyssalRiftSpawn$")
})
public abstract class EntityPlayerBossMixin extends EntityGiantZombie {
    @Unique
    private boolean eagleMixins$isAbyssalRiftSpawn;

    public EntityPlayerBossMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(
            method = "writeEntityToNBT",
            at = @At("HEAD")
    )
    public void writeEntityToNBT(NBTTagCompound compound, CallbackInfo ci) {
        compound.setBoolean("AbyssalRiftSpawn", eagleMixins$isAbyssalRiftSpawn);
    }

    @Inject(
            method = "readEntityFromNBT",
            at = @At("HEAD")
    )
    public void readEntityFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        this.eagleMixins$isAbyssalRiftSpawn = compound.getBoolean("AbyssalRiftSpawn");
    }

    public boolean abyssalRiftSpawn$isAbyssalRiftSpawn() {
        return eagleMixins$isAbyssalRiftSpawn;
    }

    public void abyssalRiftSpawn$setAbyssalRiftSpawn(boolean isAbyssalRiftSpawn) {
        this.eagleMixins$isAbyssalRiftSpawn = isAbyssalRiftSpawn;
    }
}
