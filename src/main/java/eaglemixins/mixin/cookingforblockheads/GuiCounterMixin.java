package eaglemixins.mixin.cookingforblockheads;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.blay09.mods.cookingforblockheads.client.gui.GuiCounter;
import net.blay09.mods.cookingforblockheads.tile.TileCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiCounter.class)
public abstract class GuiCounterMixin {
    @Final @Shadow(remap = false) private TileCounter tileCounter;

    @WrapOperation(
            method = "drawGuiContainerForegroundLayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/I18n;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", ordinal = 0)
    )
    public String eaglemixins_drawGuiContainerForegroundLayer(String translateKey, Object[] parameters, Operation<String> original) {
        return this.tileCounter.getDisplayName().getUnformattedText();
    }
}
