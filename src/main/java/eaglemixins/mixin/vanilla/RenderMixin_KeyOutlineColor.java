package eaglemixins.mixin.vanilla;

import eaglemixins.items.ItemLockKey;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Glowing dropped lock keys get an outline in their key color instead of white. */
@Mixin(Render.class)
public abstract class RenderMixin_KeyOutlineColor {

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void eagleMixins$keyOutlineColor(Entity entity, CallbackInfoReturnable<Integer> cir) {
        if (!(entity instanceof EntityItem) || entity.getTeam() != null) return;
        ItemStack stack = ((EntityItem) entity).getItem();
        if (stack.getItem() instanceof ItemLockKey) cir.setReturnValue(ItemLockKey.getColor(ItemLockKey.getLockId(stack)));
    }
}
