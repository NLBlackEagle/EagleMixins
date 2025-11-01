package eaglemixins.mixin.cookingforblockheads;

import net.blay09.mods.cookingforblockheads.tile.TileCounter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IWorldNameable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

@Mixin(TileCounter.class)
public abstract class TileCounterMixin_CustomName implements IWorldNameable {

    @Unique protected String eagleMixins$customName;

    @Override @Nonnull
    public String getName() {
        return this.hasCustomName() ? this.eagleMixins$customName : "container.cookingforblockheads:counter";
    }

    @Override
    public boolean hasCustomName() {
        return this.eagleMixins$customName != null && !this.eagleMixins$customName.isEmpty();
    }

    @Override @Nonnull
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    public void eaglemixins_readFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        if (compound.hasKey("CustomName", 8)) this.eagleMixins$customName = compound.getString("CustomName");
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"))
    public void eaglemixins_writeToNBT(NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> cir) {
        if (this.hasCustomName()) compound.setString("CustomName", this.eagleMixins$customName);
    }
}
