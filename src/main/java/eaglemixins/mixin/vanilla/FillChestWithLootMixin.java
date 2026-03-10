package eaglemixins.mixin.vanilla;

import eaglemixins.util.LootGenerationContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityLockableLoot.class)
public abstract class FillChestWithLootMixin {

    @Shadow private ResourceLocation lootTable;

    @Inject(method = "fillWithLoot", at = @At("HEAD"))
    private void pushTopLootTable(CallbackInfo ci) {
        if (this.lootTable != null) { // only fire if chest has loot
            LootGenerationContext.push(this.lootTable);
        }
    }

    @Inject(method = "fillWithLoot", at = @At("RETURN"))
    private void popTopLootTable(CallbackInfo ci) {
        if (this.lootTable != null) { // optional, might be null after fill
            LootGenerationContext.pop();
        }
    }
}