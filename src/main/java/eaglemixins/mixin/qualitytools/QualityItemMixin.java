package eaglemixins.mixin.qualitytools;

import com.tmtravlr.qualitytools.config.QualityItem;
import eaglemixins.util.LootTableSetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.regex.Pattern;

@Mixin(QualityItem.class)
public class QualityItemMixin implements LootTableSetter {

    // Patterns and string loot tables added by the serializer
    @Unique
    private List<Pattern> eaglemixins$loottablePatterns;

    @Unique
    private List<String> eaglemixins$loottables;

    /**
     * Intercept itemMatches to check against stored loot tables and class
     */
    @Inject(method = "itemMatches", at = @At("HEAD"), cancellable = true, remap = false)
    private void onItemMatches(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || stack.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }

        if (eaglemixins$loottables == null || eaglemixins$loottables.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }

        // Collect all loottables from the item's NBT
        Set<String> itemLootTables = new HashSet<>();
        if (stack.hasTagCompound()) {
            NBTTagCompound eagleTag = stack.getSubCompound("eaglemixins");
            if (eagleTag != null && eagleTag.hasKey("LootTable", 9)) {
                NBTTagList list = eagleTag.getTagList("LootTable", 8);
                for (int i = 0; i < list.tagCount(); i++) {
                    itemLootTables.add(list.getStringTagAt(i));
                }
            }
        }

        // Return true if ANY serializer loottable pattern matches and class matches
        boolean matched = false;
        for (int i = 0; i < eaglemixins$loottables.size(); i++) {
            String lootTableStr = eaglemixins$loottables.get(i);
            Pattern pattern = eaglemixins$loottablePatterns.get(i);

            // Check item class if this QualityItem has a class restriction
            boolean classMatches = true;
            try {
                if (((QualityItem) (Object) this).itemClass != null) {
                    classMatches = com.tmtravlr.qualitytools.QualityToolsHelper.hasClassType(
                            ((QualityItem) (Object) this).itemClass, stack.getItem().getClass()
                    );
                }
            } catch (Exception ignored) {
            }

            // Loot table match
            boolean lootMatches = itemLootTables.stream().anyMatch(t -> pattern.matcher(t).matches());

            if (classMatches && lootMatches) {
                matched = true;
                break;
            }
        }

        cir.setReturnValue(matched);
    }

    /**
     * LootTableSetter interface
     */
    @Override
    public void eaglemixins$setLootTable(net.minecraft.util.ResourceLocation rl) {
        if (eaglemixins$loottables == null) {
            eaglemixins$loottables = new ArrayList<>();
            eaglemixins$loottablePatterns = new ArrayList<>();
        }
        eaglemixins$loottables.add(rl.toString());
        eaglemixins$loottablePatterns.add(Pattern.compile(rl.toString().replace("*", ".*")));
    }

    @Override
    public void eaglemixins$setLootTables(List<net.minecraft.util.ResourceLocation> rls) {
        if (eaglemixins$loottables == null) {
            eaglemixins$loottables = new ArrayList<>();
            eaglemixins$loottablePatterns = new ArrayList<>();
        }
        for (net.minecraft.util.ResourceLocation rl : rls) {
            eaglemixins$loottables.add(rl.toString());
            eaglemixins$loottablePatterns.add(Pattern.compile(rl.toString().replace("*", ".*")));
        }
    }
}