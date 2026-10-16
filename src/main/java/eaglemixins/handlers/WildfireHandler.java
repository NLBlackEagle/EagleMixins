package eaglemixins.handlers;

import eaglemixins.compat.ModLoadedUtil;
import eaglemixins.compat.SpartanWeaponryUtil;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.WildfireConfig;
import eaglemixins.potion.PotionWildfire;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WildfireHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getAmount() <= 0) return;
        EntityLivingBase victim = event.getEntityLiving();
        if (victim == null || victim.world.isRemote) return;

        ItemStack stack = getWeaponStack(event.getSource());
        if (stack.isEmpty()) return;

        WildfireConfig.WildfireEntry entry = ForgeConfigHandler.wildfire.weapons.get(stack.getItem().getRegistryName());
        if (entry == null) return;
        if (entry.chance < 1.0F && victim.getRNG().nextFloat() >= entry.chance) return;

        victim.addPotionEffect(PotionWildfire.create(entry.level));
        if (!victim.isImmuneToFire()) victim.setFire(PotionWildfire.getFireSeconds(entry.level));
    }

    // Melee: the attacker's mainhand. Thrown Spartan weapons: the weapon carried by the projectile
    private static ItemStack getWeaponStack(DamageSource source) {
        if (source == null) return ItemStack.EMPTY;
        Entity immediate = source.getImmediateSource();
        if (immediate != null && immediate != source.getTrueSource()) {
            if (ModLoadedUtil.spartanweaponry.isLoaded()) return SpartanWeaponryUtil.getThrownWeaponStack(immediate);
            return ItemStack.EMPTY;
        }
        if (!WeaponDamageHandler.isDamageSourceAllowed(source)) return ItemStack.EMPTY;
        return ((EntityLivingBase) source.getTrueSource()).getHeldItemMainhand();
    }

    @SideOnly(Side.CLIENT)
    public static class Tooltip {
        private static final String TOOLTIP_KEY = "eaglemixins.tooltip.wildfire";

        @SubscribeEvent(priority = EventPriority.LOW)
        public static void onItemTooltip(ItemTooltipEvent event) {
            if (!ForgeConfigHandler.wildfire.showTooltip) return;
            ItemStack stack = event.getItemStack();
            if (stack.isEmpty()) return;
            ResourceLocation id = stack.getItem().getRegistryName();
            if (id == null) return;

            WildfireConfig.WildfireEntry entry = ForgeConfigHandler.wildfire.weapons.get(id);
            if (entry == null) return;

            // Per-mod lang key so each mod's tooltip style can be matched, e.g. eaglemixins.tooltip.wildfire.spartanfire
            String modKey = TOOLTIP_KEY + "." + id.getNamespace();
            String key = I18n.hasKey(modKey) ? modKey : TOOLTIP_KEY;
            event.getToolTip().add(Math.min(1, event.getToolTip().size()), I18n.format(key, entry.level));
        }
    }
}
