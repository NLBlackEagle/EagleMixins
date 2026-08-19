package eaglemixins.mixin.vanilla;

import eaglemixins.util.LootGenerationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootEntryItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LootEntryItem.class)
public abstract class LootEntryItemMixin {

    @ModifyArg(method = "addLoot", at = @At(value = "INVOKE", target = "Ljava/util/Collection;add(Ljava/lang/Object;)Z"))
    private Object eaglemixins_attachLootNBT(Object original) {
        LootGenerationContext.addTableNBT((ItemStack) original);
        return original;
    }
}