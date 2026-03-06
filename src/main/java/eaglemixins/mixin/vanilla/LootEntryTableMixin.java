package eaglemixins.mixin.vanilla;

import eaglemixins.util.LootGenerationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntryTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Mixin(LootEntryTable.class)
public abstract class LootEntryTableMixin {

    @Shadow
    private ResourceLocation table;

    @Inject(method = "addLoot", at = @At("HEAD"))
    private void pushTable(Collection<ItemStack> stacks, Random rand, LootContext context, CallbackInfo ci) {
        LootGenerationContext.push(this.table);
    }

    @Inject(method = "addLoot", at = @At("RETURN"))
    private void popTable(Collection<ItemStack> stacks, Random rand, LootContext context, CallbackInfo ci) {

        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) continue;

            NBTTagCompound tag = stack.getOrCreateSubCompound("eaglemixins");
            NBTTagList list;

            if (tag.hasKey("LootTable", 9)) { // NBTTagList exists
                list = tag.getTagList("LootTable", 8);
            } else {
                list = new NBTTagList();
                tag.setTag("LootTable", list);
            }

            Set<String> existing = new HashSet<>();
            for (int i = 0; i < list.tagCount(); i++) existing.add(list.getStringTagAt(i));

            for (ResourceLocation rl : LootGenerationContext.getCurrentStack()) {
                if (existing.add(rl.toString())) {
                    list.appendTag(new NBTTagString(rl.toString()));
                }
            }
        }

        LootGenerationContext.pop();
    }
}