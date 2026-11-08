package eaglemixins.mixin.fishsundeadrising;

import com.Fishmod.mod_LavaCow.entities.EntityParasite;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A FUR parasite attached to a player rides them and sits in front of their face.
 * The client can only hit an entity riding the viewer when the viewer's eye is INSIDE that entity's hitbox
 * (EntityRenderer.getMouseOver ignores ray hits on entities in the viewer's own riding chain).
 * FUR places the parasite at mountedYOffset + (height / 2 - 0.85), which only covers the eye for player heights
 * between ~1.375 and ~2.125 blocks. Trinkets and Baubles races resize the player (dragon 120% = 2.16, fairies,
 * dwarves, titans...), leaving the eye outside the parasite's hitbox, so it can't be hit.
 * This clamps the parasite's vertical position so its hitbox always contains the rider's eye.
 * For normal sized players FUR's original position already does, so nothing changes there.
 */
@Mixin(EntityParasite.class)
public abstract class EntityParasiteMixin extends EntityLiving {

    private static final double EAGLEMIXINS_EYE_MARGIN = 0.05D;

    public EntityParasiteMixin(World worldIn) {
        super(worldIn);
    }

    @ModifyReturnValue(method = "getYOffset", at = @At("RETURN"))
    private double eaglemixins_furParasite_keepEyeInHitbox(double yOffset) {
        Entity mount = this.getRidingEntity();
        if (!(mount instanceof EntityPlayer)) return yOffset;

        double mountedOffset = mount.getMountedYOffset();
        double eye = mount.getEyeHeight();
        double margin = Math.min(EAGLEMIXINS_EYE_MARGIN, this.height / 2.0D);
        // Parasite feet relative to the player's feet, clamped so [feet, feet + height] contains the eye
        double feet = MathHelper.clamp(mountedOffset + yOffset, eye - this.height + margin, eye - margin);
        return feet - mountedOffset;
    }
}
