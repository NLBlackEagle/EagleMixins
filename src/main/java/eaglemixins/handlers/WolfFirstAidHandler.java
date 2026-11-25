package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.WolfPackConfig;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.network.MessageUpdatePart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * First Aid only aims melee hits of EntityMob subclasses. Wolves are animals, so their bites land on a random body part
 * (head included) and can kill outright. This moves bite damage from parts outside "Wolf Bite Parts" onto those parts.
 */
public class WolfFirstAidHandler {

    private static String[] parsedFrom = null;
    private static Set<EnumPlayerPart> biteParts = EnumSet.noneOf(EnumPlayerPart.class);

    @SubscribeEvent
    public static void onFirstAidDamage(ichttt.mods.firstaid.api.event.FirstAidLivingDamageEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null || player.world.isRemote) return;
        WolfPackConfig cfg = ForgeConfigHandler.wolfpack;
        if (!cfg.enabled || !cfg.firstAidRedirectBites) return;

        Entity source = event.getSource().getTrueSource();
        if (!WolfPackHandler.isWolf(source) || ((EntityWolf) source).isTamed()) return;
        if (event.getSource().getImmediateSource() != source) return; // only bites

        Set<EnumPlayerPart> allowed = getBiteParts();
        if (allowed.isEmpty()) return;

        AbstractPlayerDamageModel before = event.getBeforeDamage();
        AbstractPlayerDamageModel after = event.getAfterDamage();
        Set<AbstractDamageablePart> changed = new LinkedHashSet<>();

        float moved = 0.0F;
        for (EnumPlayerPart part : EnumPlayerPart.VALUES) {
            if (allowed.contains(part)) continue;
            AbstractDamageablePart afterPart = after.getFromEnum(part);
            float delta = before.getFromEnum(part).currentHealth - afterPart.currentHealth;
            if (delta <= 0.0F) continue;
            afterPart.currentHealth += delta;
            moved += delta;
            changed.add(afterPart);
        }
        if (moved <= 0.0F) return;

        List<EnumPlayerPart> targets = new ArrayList<>(allowed);
        Collections.shuffle(targets, player.getRNG());
        for (EnumPlayerPart part : targets) {
            if (moved <= 0.0F) break;
            AbstractDamageablePart target = after.getFromEnum(part);
            float left = target.damage(moved, player, true, 0.0F);
            if (left < moved) changed.add(target);
            moved = left;
        } // whatever doesn't fit anywhere is dropped instead of hitting the head/body

        if (player instanceof EntityPlayerMP) {
            for (AbstractDamageablePart part : changed) {
                FirstAid.NETWORKING.sendTo(new MessageUpdatePart(part), (EntityPlayerMP) player);
            }
        }
    }

    private static Set<EnumPlayerPart> getBiteParts() {
        String[] config = ForgeConfigHandler.wolfpack.firstAidBiteParts;
        if (config != parsedFrom) {
            Set<EnumPlayerPart> parts = EnumSet.noneOf(EnumPlayerPart.class);
            for (String name : config) {
                try {
                    parts.add(EnumPlayerPart.valueOf(name.trim().toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                }
            }
            biteParts = parts;
            parsedFrom = config;
        }
        return biteParts;
    }
}
