// src/main/java/eaglemixins/handlers/FirstAidRadiationRouter.java
package eaglemixins.handlers;

import eaglemixins.util.RadiationDamageSource;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.api.event.FirstAidLivingDamageEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class FirstAidRadiationHandler {

    private static final boolean DEBUG = false;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFirstAidDamage(FirstAidLivingDamageEvent event) {
        final EntityPlayer player = event.getEntityPlayer();
        if (player == null || player.world.isRemote) return; // only mutate server state

        final DamageSource src = event.getSource();
        // Be permissive about matching the source
        final String type = src == null ? "" : src.getDamageType();
        if (src != RadiationDamageSource.RADIATION && !"radiation".equalsIgnoreCase(type)) return;

        final AbstractPlayerDamageModel before = event.getBeforeDamage();
        final AbstractPlayerDamageModel after  = event.getAfterDamage();

        // Reconstruct full incoming amount: (damage FA already applied) + (leftover it didn't assign)
        final float pre  = before.getCurrentHealth(); // deprecated but present
        final float post = after.getCurrentHealth();
        float incoming = Math.max(0f, pre - post) + event.getUndistributedDamage();
        if (incoming <= 0f) return;

        if (DEBUG) {
            System.out.println("[RadRouter] src=" + type + " pre=" + pre + " post=" + post +
                    " leftover=" + event.getUndistributedDamage() + " incoming=" + incoming);
        }

        // 1) Reset "after" back to "before" (per-part via NBT copy)
        for (EnumPlayerPart part : EnumPlayerPart.values()) {
            AbstractDamageablePart b = before.getFromEnum(part);
            AbstractDamageablePart a = after.getFromEnum(part);
            NBTTagCompound snap = b.serializeNBT();
            a.deserializeNBT(snap);
        }

        // 2) Apply our survival-friendly routing: limbs/feet → body → head(last with 1 HP floor)
        final boolean applyDebuffs = true; // server side
        incoming = damagePart(after, EnumPlayerPart.LEFT_ARM,    player, incoming, applyDebuffs, 0f);
        incoming = damagePart(after, EnumPlayerPart.RIGHT_ARM,   player, incoming, applyDebuffs, 0f);
        incoming = damagePart(after, EnumPlayerPart.LEFT_LEG,    player, incoming, applyDebuffs, 0f);
        incoming = damagePart(after, EnumPlayerPart.RIGHT_LEG,   player, incoming, applyDebuffs, 0f);
        incoming = damagePart(after, EnumPlayerPart.LEFT_FOOT,   player, incoming, applyDebuffs, 0f);
        incoming = damagePart(after, EnumPlayerPart.RIGHT_FOOT,  player, incoming, applyDebuffs, 0f);
        incoming = damagePart(after, EnumPlayerPart.BODY,        player, incoming, applyDebuffs, 0f);
        incoming = damagePart(after, EnumPlayerPart.HEAD,        player, incoming, applyDebuffs, 1.0f); // head last

        if (DEBUG) {
            float newPost = after.getCurrentHealth();
            System.out.println("[RadRouter] newPost=" + newPost + " leftoverAfterRouting=" + incoming);
        }

        // IMPORTANT: do NOT cancel the event. First Aid will now commit 'after' and sync it.

    }

    private static float damagePart(AbstractPlayerDamageModel model, EnumPlayerPart part,
                                    EntityPlayer player, float amt, boolean debuffs, float minHealth) {
        if (amt <= 0f) return 0f;
        AbstractDamageablePart p = model.getFromEnum(part);
        // returns leftover (not-fitting) amount to pass to the next part
        return p.damage(amt, player, debuffs, minHealth);
    }
}
