package eaglemixins.mixin.simpledifficulty;

import com.charles445.simpledifficulty.api.thirst.ThirstEnum;
import com.charles445.simpledifficulty.api.thirst.ThirstEnumBlockPos;
import com.charles445.simpledifficulty.util.internal.ThirstUtilInternal;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThirstUtilInternal.class)
public class ThirstBlockMixin {

    @ModifyReturnValue(method = "traceWater", at = @At(value = "RETURN", ordinal = 3), remap = false)
    private ThirstEnumBlockPos eaglemixins_extendDrinkableBlocks(ThirstEnumBlockPos original, EntityPlayer player, @Local RayTraceResult trace, @Local Block traceBlock) {
        if (original != null) return original; //already returned purified water

        if(traceBlock.getRegistryName() == null) return null;
        return ForgeConfigHandler.cachedDrinkableBlocks.contains(traceBlock.getRegistryName().toString()) ? new ThirstEnumBlockPos(ThirstEnum.NORMAL, trace.getBlockPos()) : null;
    }
}