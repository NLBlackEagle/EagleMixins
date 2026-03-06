package eaglemixins.mixin.vanilla;

import eaglemixins.util.LootGenerationContext;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LootTable.class)
public abstract class LootTableMixin {

    @Unique
    private ResourceLocation eaglemixins$topTable;

    @Inject(method = "fillInventory", at = @At("HEAD"))
    private void pushTopTable(IInventory inv, Random rand, LootContext ctx, CallbackInfo ci) {
        // If someone set the top table externally
        if (this.eaglemixins$topTable != null) {
            LootGenerationContext.push(this.eaglemixins$topTable);
        }
    }

    @Inject(method = "fillInventory", at = @At("RETURN"))
    private void tagGeneratedItems(IInventory inventory, java.util.Random rand, LootContext context, CallbackInfo ci) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            NBTTagCompound tag = stack.getOrCreateSubCompound("eaglemixins");

            if (!tag.hasKey("LootTable", 9)) {
                NBTTagList list = new NBTTagList();
                for (net.minecraft.util.ResourceLocation rl : LootGenerationContext.getCurrentStack()) {
                    list.appendTag(new NBTTagString(rl.toString()));
                }
                tag.setTag("LootTable", list);
            }
        }
        LootGenerationContext.pop(); // pop the top-level table
    }
}