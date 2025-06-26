package eaglemixins.mixin.cookingforblockheads;

import net.blay09.mods.cookingforblockheads.tile.TileCounter;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.storage.loot.ILootContainer;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileCounter.class)
@Implements({
        @Interface(iface = ILootContainer.class, prefix = "lootContainer$")
})
public abstract class TileCounterMixin extends TileEntity {

    @Unique
    protected ResourceLocation eagleMixins$lootTable;

    public ResourceLocation lootContainer$getLootTable() {
        ResourceLocation lootTable = this.eagleMixins$lootTable;
        this.eagleMixins$lootTable = null;
        return lootTable;
    }

    @Inject(
            method = "readFromNBT",
            at = @At("RETURN")
    )
    public void readFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        if (compound.hasKey("LootTable", 8)) {
            this.eagleMixins$lootTable = new ResourceLocation(compound.getString("LootTable"));
        }
    }

    @Inject(
            method = "writeToNBT",
            at = @At("RETURN")
    )
    public void writeToNBT(NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> cir) {
        if (this.eagleMixins$lootTable != null) {
            compound.setString("LootTable", this.eagleMixins$lootTable.toString());
        }
    }
}
