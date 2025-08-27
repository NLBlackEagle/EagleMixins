package eaglemixins.mixin.lycanitesmobs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.lycanitesmobs.core.spawner.Spawner;
import com.lycanitesmobs.core.spawner.trigger.BlockSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.CropBlockSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.OreBlockSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.TreeBlockSpawnTrigger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = {
        CropBlockSpawnTrigger.class,
        OreBlockSpawnTrigger.class,
        TreeBlockSpawnTrigger.class
})
public abstract class BlockSpawnTriggersInheritBlacklistMixin extends BlockSpawnTrigger {

    public BlockSpawnTriggersInheritBlacklistMixin(Spawner spawner) {
        super(spawner);
    }

    @ModifyReturnValue(
            method = "isTriggerBlock",
            at = @At("RETURN"),
            remap = false
    )
    public boolean eaglemixins_lycanitesMobsBlockSpawnTriggers_isTriggerBlockSuper(boolean original, IBlockState blockState, World world, BlockPos blockPos, int fortune){
        return original && super.isTriggerBlock(blockState, world, blockPos, fortune);
    }
}
