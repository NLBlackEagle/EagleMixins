package eaglemixins.mixin.cookingforblockheads;

import net.blay09.mods.cookingforblockheads.tile.TileCounter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IWorldNameable;
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
        @Interface(iface = ILootContainer.class, prefix = "lootContainer$"),
        @Interface(iface = IWorldNameable.class, prefix = "worldNameable$"),
})
public abstract class TileCounterMixin extends TileEntity implements eaglemixins.util.LootTableSetter {

    @Unique
    protected ResourceLocation eagleMixins$lootTable;

    @Unique
    protected String eagleMixins$customName;


    public ResourceLocation lootContainer$getLootTable() {
        ResourceLocation lootTable = this.eagleMixins$lootTable;
        return lootTable;
    }

    @Override
    public void eaglemixins$setLootTable(@javax.annotation.Nullable ResourceLocation rl) {
        this.eagleMixins$lootTable = rl;
    }

    public String worldNameable$getName() {
        return this.worldNameable$hasCustomName() ? this.eagleMixins$customName : "container.cookingforblockheads:counter";
    }

    public boolean worldNameable$hasCustomName() {
        return this.eagleMixins$customName != null && !this.eagleMixins$customName.isEmpty();
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.worldNameable$hasCustomName() ? new TextComponentString(this.worldNameable$getName()) : new TextComponentTranslation(this.worldNameable$getName());
    }

    @Inject(
            method = "readFromNBT",
            at = @At("RETURN")
    )
    public void readFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        if (compound.hasKey("LootTable", 8)) {
            this.eagleMixins$lootTable = new ResourceLocation(compound.getString("LootTable"));
        }
        if (compound.hasKey("CustomName", 8)) {
            this.eagleMixins$customName = compound.getString("CustomName");
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
        if (this.worldNameable$hasCustomName()) {
            compound.setString("CustomName", this.eagleMixins$customName);
        }
    }
}
