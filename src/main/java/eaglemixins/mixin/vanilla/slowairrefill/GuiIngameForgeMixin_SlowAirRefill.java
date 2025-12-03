package eaglemixins.mixin.vanilla.slowairrefill;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiIngameForge.class)
public abstract class GuiIngameForgeMixin_SlowAirRefill {

    @ModifyExpressionValue(
            method = "renderAir",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isInsideOfMaterial(Lnet/minecraft/block/material/Material;)Z")
    )
    private boolean eaglemixins_gradualAirRefill(boolean original, @Local(name = "player") EntityPlayer player) {
        return original || player.getAir() < 300;
    }
}
