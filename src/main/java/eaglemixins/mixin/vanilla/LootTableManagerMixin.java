package eaglemixins.mixin.vanilla;

import eaglemixins.debug.LootDebug;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootTableManager.class)
public abstract class LootTableManagerMixin {
    @Inject(method = "getLootTableFromLocation", at = @At("RETURN"))
    private void eagle$rememberId(ResourceLocation id, CallbackInfoReturnable<LootTable> cir) {
        LootTable table = cir.getReturnValue();
        if (table != null) LootDebug.LT_IDS.put(table, id);
    }
}