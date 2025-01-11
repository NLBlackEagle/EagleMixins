package eaglemixins.mixin.vanilla;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityDropItemMixin {
    @Inject(
            method = "entityDropItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityItem;<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V"),
            cancellable = true
    )
    void stopDisarmingSentientDragonbone(ItemStack stack, float offsetY, CallbackInfoReturnable<EntityItem> cir) {
        //Players are allowed to drop their weapons or get their weapons disarmed
        if ((Entity) (Object) this instanceof EntityPlayer) return;

        ResourceLocation resourceLocation = stack.getItem().getRegistryName();
        if (resourceLocation == null) return;
        String itemId = resourceLocation.toString();

        if (itemId.contains("living") || itemId.contains("sentient") || itemId.contains("dragonbone"))
            //Dont drop anything
            cir.setReturnValue(null);
    }
}