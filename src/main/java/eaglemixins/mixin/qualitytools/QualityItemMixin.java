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

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Pattern;

@Mixin(value = QualityItem.class, remap = false)
public class QualityItemMixin implements LootTableSetter {

    @Unique
    private List<Pattern> eaglemixins$loottablePatterns;

    @Unique
    private List<ResourceLocation> eaglemixins$loottables;

    @Inject(method = "itemMatches", at = @At("HEAD"), cancellable = true)
    private void onItemMatches(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || stack.isEmpty()) return;

        try {
            List<?> whitelist = (List<?>) QualityItem.class.getField("whitelist").get(this);
            if (whitelist == null || whitelist.isEmpty()) return;

            // Read NBT loottables into a set for fast lookup
            Set<String> itemLootTables = new HashSet<>();
            if (stack.hasTagCompound()) {
                NBTTagCompound eagleTag = stack.getSubCompound("eaglemixins");
                if (eagleTag != null && eagleTag.hasKey("LootTable", 9)) {
                    NBTTagList lootTables = eagleTag.getTagList("LootTable", 8);
                    for (int i = 0; i < lootTables.tagCount(); i++) {
                        itemLootTables.add(lootTables.getStringTagAt(i));
                    }
                }
            }

            boolean matched = false;

            for (Object entry : whitelist) {
                List<String> entryLootTables = new ArrayList<>();
                String entryClass = null;

                try {
                    entryClass = (String) entry.getClass().getField("class").get(entry);
                } catch (NoSuchFieldException ignored) {}

                try {
                    Object loot = entry.getClass().getField("loottable").get(entry);
                    if (loot instanceof String) entryLootTables.add((String) loot);
                    else if (loot instanceof List) entryLootTables.addAll((List<String>) loot);
                } catch (NoSuchFieldException ignored) {}

                // Check class if defined
                boolean classMatches = true;
                if (entryClass != null) {
                    classMatches = QualityToolsHelper.hasClassType(entryClass, stack.getItem().getClass());
                }

                // Check loot table if defined: any match counts
                boolean lootMatches = entryLootTables.isEmpty() || entryLootTables.stream().anyMatch(itemLootTables::contains);

                if (classMatches && lootMatches) {
                    matched = true;
                    break;
                }
            }

            cir.setReturnValue(matched);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            eaglemixins$loottablePatterns = new ArrayList<>();
        }
        for (ResourceLocation rl : rls) {
            eaglemixins$loottables.add(rl);
            eaglemixins$loottablePatterns.add(Pattern.compile(rl.toString().replace("*", ".*")));
        }
    }
}