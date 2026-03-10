package eaglemixins.mixin.vanilla;

import eaglemixins.util.LootGenerationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntryItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Mixin(LootEntryItem.class)
public abstract class LootEntryItemMixin {

    @Inject(method = "addLoot", at = @At("RETURN"))
    private void eaglemixins$attachLootNBT(Collection<ItemStack> stacks, Random rand, LootContext context, CallbackInfo ci) {

        for (ItemStack stack : stacks) {

            if (stack.isEmpty()) continue;

            NBTTagCompound tag = stack.getOrCreateSubCompound("eaglemixins");

            NBTTagList list;
            if (tag.hasKey("LootTable", 9)) {
                list = tag.getTagList("LootTable", 8);
            } else {
                list = new NBTTagList();
                tag.setTag("LootTable", list);
            }

            Set<String> existing = new HashSet<>();
            for (int i = 0; i < list.tagCount(); i++) {
                existing.add(list.getStringTagAt(i));
            }

            for (ResourceLocation rl : LootGenerationContext.getCurrentStack()) {

                String s = rl.toString();

                if (existing.add(s)) {
                    list.appendTag(new NBTTagString(s));
                }
            }
        }
    }
}