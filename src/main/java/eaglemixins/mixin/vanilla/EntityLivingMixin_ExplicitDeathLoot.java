package eaglemixins.mixin.vanilla;

import eaglemixins.handlers.util.ExplicitDeathLoot;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLiving.class)
public abstract class EntityLivingMixin_ExplicitDeathLoot implements ExplicitDeathLoot {
    @Shadow private ResourceLocation deathLootTable;
    @Unique private boolean eaglemixins$rolledExplicitDeathLoot;

    @Inject(method = "dropLoot", at = @At("HEAD"))
    private void eaglemixins$captureExplicitDeathLoot(CallbackInfo ci) {
        eaglemixins$rolledExplicitDeathLoot = deathLootTable != null;
    }

    @Override
    public boolean eaglemixins$hasExplicitDeathLoot() {
        return eaglemixins$rolledExplicitDeathLoot || deathLootTable != null;
    }
}
