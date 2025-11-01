package eaglemixins.mixin.cookingforblockheads;

import eaglemixins.util.LootTableSetter;
import net.blay09.mods.cookingforblockheads.tile.TileCounter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.ILootContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mixin(TileCounter.class)
public abstract class TileCounterMixin_LootTables implements LootTableSetter, ILootContainer {
    @Unique protected ResourceLocation eagleMixins$lootTable;

    @Override @Nonnull
    public ResourceLocation getLootTable() {
        return this.eagleMixins$lootTable;
    }

    @Override
    public void eaglemixins$setLootTable(@Nullable ResourceLocation rl) {
        this.eagleMixins$lootTable = rl;
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    public void eaglemixins_readFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        if (compound.hasKey("LootTable", 8)) this.eagleMixins$lootTable = new ResourceLocation(compound.getString("LootTable"));
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"))
    public void eaglemixins_writeToNBT(NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> cir) {
        if (this.eagleMixins$lootTable != null) compound.setString("LootTable", this.eagleMixins$lootTable.toString());
    }
}
