package eaglemixins.mixin.qualitytools;

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
        if (stack == null || stack.isEmpty()) return;
        if (eaglemixins$loottables == null || eaglemixins$loottables.isEmpty()) return;

        List<String> itemLootTables = new ArrayList<>();

        if (stack.hasTagCompound()) {
            NBTTagCompound eagleTag = stack.getSubCompound("eaglemixins");
            if (eagleTag != null && eagleTag.hasKey("LootTable", 9)) { // singular key matches mixin
                NBTTagList lootTables = eagleTag.getTagList("LootTable", 8);
                for (int i = 0; i < lootTables.tagCount(); i++) {
                    itemLootTables.add(lootTables.getStringTagAt(i));
                }
            }
        }

        // Return true if ANY serializer loottable is found in the item's NBT
        boolean matches = eaglemixins$loottables.stream().anyMatch(rl -> {
            String pattern = rl.toString().replace("*", ".*");
            return itemLootTables.stream().anyMatch(t -> t.matches(pattern));
        });

        cir.setReturnValue(matches);
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