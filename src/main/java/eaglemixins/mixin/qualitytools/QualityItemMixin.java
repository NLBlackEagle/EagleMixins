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

import java.util.*;
import java.util.regex.Pattern;

@Mixin(QualityItem.class)
public class QualityItemMixin implements LootTableSetter {

    @Unique
    private List<String> eaglemixins$loottables;

    @Unique
    private List<Pattern> eaglemixins$loottablePatterns;

    @Unique
    private void eaglemixins$initLootTables() {
        if (eaglemixins$loottables == null) {
            eaglemixins$loottables = new ArrayList<>();
            eaglemixins$loottablePatterns = new ArrayList<>();
        }
    }


    @Override
    public void eaglemixins$addLootTable(ResourceLocation rl) {
        eaglemixins$initLootTables();

        String s = rl.toString();
        eaglemixins$loottables.add(s);
        eaglemixins$loottablePatterns.add(Pattern.compile("^" + s.replace("*", ".*") + ".*$"));
    }

    @Inject(method = "itemMatches", at = @At("HEAD"), cancellable = true, remap = false)
    private void onItemMatches(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {

        if (stack == null || stack.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }

        if (eaglemixins$loottablePatterns == null || eaglemixins$loottablePatterns.isEmpty()) {
            return;
        }

        QualityItem self = (QualityItem) (Object) this;
        if (!eaglemixins$classMatches(self, stack)) {
            cir.setReturnValue(false);
            return;
        }

        Set<String> itemLootTables = eaglemixins$getItemLootTables(stack);

        for (Pattern pattern : eaglemixins$loottablePatterns) {
            for (String table : itemLootTables) {

                if (pattern.matcher(table).matches()) {
                    cir.setReturnValue(true);
                    eaglemixins$clean(stack);
                    return;
                }
            }
        }
        cir.setReturnValue(false);
    }

    @Unique
    private Set<String> eaglemixins$getItemLootTables(ItemStack stack) {

        Set<String> tables = new HashSet<>();

        if (!stack.hasTagCompound()) {
            return tables;
        }

        NBTTagCompound eagleTag = stack.getSubCompound("eaglemixins");
        if (eagleTag == null || !eagleTag.hasKey("LootTable", 9)) {
            return tables;
        }

        NBTTagList list = eagleTag.getTagList("LootTable", 8);

        for (int i = 0; i < list.tagCount(); i++) {
            tables.add(list.getStringTagAt(i));
        }

        return tables;
    }

    @Unique
    private boolean eaglemixins$classMatches(QualityItem item, ItemStack stack) {

        try {
            if (item.itemClass == null) {
                return true;
            }

            return com.tmtravlr.qualitytools.QualityToolsHelper.hasClassType(
                    item.itemClass,
                    stack.getItem().getClass()
            );
        } catch (Exception ignored) {
            return true;
        }
    }

    @Unique
    public static void eaglemixins$clean(ItemStack stack) {
        if (stack.isEmpty()) return;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) return;

        if (tag.hasKey("eaglemixins")) {
            tag.removeTag("eaglemixins");

            if (tag.isEmpty()) {
                stack.setTagCompound(null);
            }
        }
    }
}