package eaglemixins.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import eaglemixins.util.LootGenerationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntryTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Random;

@Mixin(LootEntryTable.class)
public abstract class LootEntryTableMixin {

    @Shadow @Final protected ResourceLocation table;

    @WrapMethod(method = "addLoot")
    private void eaglemixins_pushTable(Collection<ItemStack> stacks, Random rand, LootContext context, Operation<Void> original) {
        if (this.table != null) { // this should always be true but you never know
            LootGenerationContext.push(this.table);
            original.call(stacks, rand, context);
            LootGenerationContext.pop();
        } else
            original.call(stacks, rand, context);
    }
}