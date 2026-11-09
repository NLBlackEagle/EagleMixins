package eaglemixins.mixin.xat;

import eaglemixins.config.ForgeConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import xzeroair.trinkets.races.EntityRacePropertiesHandler;

/**
 * Trinkets and Baubles races only shrink the player to 92% height while crouching.
 * For tall races (dragon 120% = 2.16 blocks) that is 1.99 blocks, so they can't fit through
 * 2 high gaps with a trapdoor (1.81) and only barely through plain 2 high gaps.
 * This uses a configurable multiplier instead, for races taller than a normal player only.
 * The eye height is derived from the same adjusted height, so the camera follows.
 */
@Mixin(value = EntityRacePropertiesHandler.class, remap = false)
public abstract class RaceCrouchHeightMixin {

    @Shadow
    public abstract float getHeight();

    @ModifyConstant(method = "getAdjustedHeight", constant = @Constant(floatValue = 0.92F))
    private float eaglemixins$tallRaceCrouchHeight(float original) {
        if (this.getHeight() <= 1.8F) return original;
        return (float) ForgeConfigHandler.server.tallRaceCrouchHeight;
    }
}
