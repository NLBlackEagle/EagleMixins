package eaglemixins.mixin.vanilla;

import eaglemixins.util.LootGenerationContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ResourceLocation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityLockableLoot.class)
public abstract class FillChestWithLootMixin {

    @Shadow private ResourceLocation lootTable;

    @Unique private ResourceLocation eaglemixins$captureLootTable;
    @Unique private boolean eaglemixins$processedLoot = false;

    @Inject(method = "fillWithLoot", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/storage/loot/LootTable;fillInventory(Lnet/minecraft/inventory/IInventory;Ljava/util/Random;Lnet/minecraft/world/storage/loot/LootContext;)V"
    ))
    private void eaglemixins$pushContext(EntityPlayer player, CallbackInfo ci) {
        if (this.lootTable != null) {
            LootGenerationContext.push(this.lootTable);
        }
    }

    @Inject(method = "fillWithLoot", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/tileentity/TileEntityLockableLoot;lootTable:Lnet/minecraft/util/ResourceLocation;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.BEFORE
    ))
    private void eaglemixins$capture(EntityPlayer player, CallbackInfo ci) {
        this.eaglemixins$captureLootTable = this.lootTable;
        this.eaglemixins$processedLoot = false;
    }

    @Inject(method = "fillWithLoot", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/storage/loot/LootTable;fillInventory(Lnet/minecraft/inventory/IInventory;Ljava/util/Random;Lnet/minecraft/world/storage/loot/LootContext;)V",
            shift = At.Shift.AFTER
    ))
    private void eaglemixins$afterLootGenerated(EntityPlayer player, CallbackInfo ci) {
        if (eaglemixins$processedLoot) return;
        eaglemixins$processedLoot = true;

        if (this.eaglemixins$captureLootTable == null) return;

        TileEntityLockableLoot self = (TileEntityLockableLoot) (Object) this;

        for (int i = 0; i < self.getSizeInventory(); i++) {
            ItemStack stack = self.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            NBTTagCompound tag = stack.getOrCreateSubCompound("eaglemixins");
            NBTTagList list = tag.hasKey("LootTables", 9)
                    ? tag.getTagList("LootTables", 8)
                    : new NBTTagList();

            list.appendTag(new net.minecraft.nbt.NBTTagString(eaglemixins$captureLootTable.toString()));
            tag.setTag("LootTables", list);

                System.out.println("[EagleMixins] Tagged for quality -> " + stack.getDisplayName());
        }

        LootGenerationContext.pop();
    }
}