package eaglemixins.mixin.qualitytools;

import com.tmtravlr.qualitytools.config.QualityItem;
import eaglemixins.util.LootGenerationContext;
import eaglemixins.util.LootTableSetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(QualityItem.class)
public class QualityItemMixin implements LootTableSetter {

    @Unique private final Set<String> eaglemixins$loottables = new HashSet<>();

    @Override
    public void eaglemixins$setOrAddLootTable(ResourceLocation loc) {
        String id = loc.toString();
        eaglemixins$loottables.add(id);
    }

    @Inject(
            method = "itemMatches",
            at = @At(value = "FIELD", target = "Lcom/tmtravlr/qualitytools/config/QualityItem;meta:I", ordinal = 0),
            cancellable = true,
            remap = false
    )
    private void eaglemixins_checkLootTableCondition(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(!eaglemixins$loottables.isEmpty() &&
                // none of the tags saved in stack NBT match the ones required here -> don't consider this stack for this quality
                LootGenerationContext.getTablesFromStack(stack).stream().noneMatch(eaglemixins$loottables::contains)
        )
            cir.setReturnValue(false);
    }
}