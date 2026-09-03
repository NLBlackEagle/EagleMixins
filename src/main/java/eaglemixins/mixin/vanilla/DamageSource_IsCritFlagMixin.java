package eaglemixins.mixin.vanilla;

import eaglemixins.util.IDamageSource_IsCritFlagMixin;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
public abstract class DamageSource_IsCritFlagMixin implements IDamageSource_IsCritFlagMixin {

    @Unique
    private boolean eagleMixins$isCrit = false;

    @Unique
    @Override
    public void eagleMixins$setCrit(boolean isCrit) {
        this.eagleMixins$isCrit = isCrit;
    }

    @Unique
    @Override
    public boolean eagleMixins$isCrit() {
        return this.eagleMixins$isCrit;
    }
}
