package eaglemixins.mixin.qualitytools;

import com.tmtravlr.qualitytools.QualityToolsHelper;
import com.tmtravlr.qualitytools.config.QualityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.ArrayList;

@Mixin(value = QualityItem.class, remap = false)
public class QualityItemMixin {

    @Inject(method = "itemMatches", at = @At("HEAD"), cancellable = true)
    private void onItemMatches(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || stack.isEmpty()) return;

        try {
            // Access the whitelist from QualityItem
            List<?> whitelist = (List<?>) QualityItem.class.getField("whitelist").get(this);
            if (whitelist == null || whitelist.isEmpty()) return;

            // Collect loot tables from NBT (if any)
            List<String> itemLootTables = new ArrayList<>();
            if (stack.hasTagCompound()) {
                NBTTagCompound eagleTag = stack.getSubCompound("eaglemixins");
                if (eagleTag != null && eagleTag.hasKey("LootTables", 9)) {
                    NBTTagList lootTables = eagleTag.getTagList("LootTables", 8);
                    for (int i = 0; i < lootTables.tagCount(); i++) {
                        String table = lootTables.getStringTagAt(i);
                        itemLootTables.add(table);
                        System.out.println("[DEBUG] Item " + stack.getDisplayName() + " has loot table: " + table);
                    }
                }
            }

            boolean matched = false;

            for (Object entry : whitelist) {
                // Attempt to read class and loottable fields from the entry
                String entryClass = null;
                String entryLootTable = null;

                try {
                    entryClass = (String) entry.getClass().getField("class").get(entry);
                } catch (NoSuchFieldException ignored) {}
                try {
                    entryLootTable = (String) entry.getClass().getField("loottable").get(entry);
                } catch (NoSuchFieldException ignored) {}

                // Check class if defined
                boolean classMatches = true;
                if (entryClass != null) {
                    classMatches = QualityToolsHelper.hasClassType(entryClass, stack.getItem().getClass());
                }

                // Check loot table if defined
                boolean lootMatches = true;
                if (entryLootTable != null) {
                    lootMatches = itemLootTables.contains(entryLootTable);
                }

                if (classMatches && lootMatches) {
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                cir.setReturnValue(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}