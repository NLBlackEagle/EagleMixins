package eaglemixins.mixin.qualitytools;

import com.tmtravlr.qualitytools.QualityToolsHelper;
import com.tmtravlr.qualitytools.config.QualityItem;
import eaglemixins.util.LootTableSetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.ArrayList;

@Mixin(value = QualityItem.class, remap = false)
public class QualityItemMixin implements LootTableSetter {

    @Unique
    private List<ResourceLocation> eaglemixins$loottables;

    @Inject(method = "itemMatches", at = @At("HEAD"), cancellable = true)
    private void onItemMatches(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {

        if (stack == null || stack.isEmpty())
            return;

        if (eaglemixins$loottables == null || eaglemixins$loottables.isEmpty())
            return;

        List<String> itemLootTables = new ArrayList<>();

        if (stack.hasTagCompound()) {
            NBTTagCompound eagleTag = stack.getSubCompound("eaglemixins");

            if (eagleTag != null && eagleTag.hasKey("LootTables", 9)) {
                NBTTagList lootTables = eagleTag.getTagList("LootTables", 8);

                for (int i = 0; i < lootTables.tagCount(); i++) {
                    itemLootTables.add(lootTables.getStringTagAt(i));
                }
            }
        }

        // Check if any of the QualityItem loottables match
        for (ResourceLocation rl : eaglemixins$loottables) {
            String pattern = rl.toString().replace("*", ".*"); // wildcard support
            boolean match = itemLootTables.stream().anyMatch(t -> t.matches(pattern));
            if (match) return; // found a match, item is valid
        }

        cir.setReturnValue(false); // no match found
    }

    @Override
    public void eaglemixins$setLootTable(ResourceLocation rl) {
        if (eaglemixins$loottables == null) {
            eaglemixins$loottables = new ArrayList<>();
        }
        eaglemixins$loottables.add(rl);
    }

    @Override
    public void eaglemixins$setLootTables(List<ResourceLocation> rls) {
        if (eaglemixins$loottables == null) {
            eaglemixins$loottables = new ArrayList<>();
        }
        eaglemixins$loottables.addAll(rls);
    }
}