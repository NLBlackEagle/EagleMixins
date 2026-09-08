package eaglemixins.handlers;

import com.oblivioussp.spartanweaponry.api.IWeaponPropertyContainer;
import com.oblivioussp.spartanweaponry.api.weaponproperty.WeaponProperty;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Documents the inverse Two-Handed buff (see WeaponPropertyTwoHanded_BuffMixin) on the weapon's tooltip,
 * the same way Spartan Weaponry documents the Two-Handed debuff.
 */
public class SpartanTwoHandedBuffHandler {

    private static final String TWO_HANDED_TYPE = "two_handed";

    @SubscribeEvent(priority = EventPriority.LOW)
    @SideOnly(Side.CLIENT)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof IWeaponPropertyContainer)) return;
        if (!GuiScreen.isShiftKeyDown()) return; // match the debuff's own shift-to-reveal rule

        IWeaponPropertyContainer<?> container = (IWeaponPropertyContainer<?>) stack.getItem();
        int level = findTwoHandedLevel(container.getAllWeaponPropertiesWithType(TWO_HANDED_TYPE));
        if (level == 0) level = findTwoHandedLevel(container.getMaterialEx().getAllWeaponPropertiesWithType(TWO_HANDED_TYPE));
        if (level == 0) return;

        float buff = level >= 2 ? ForgeConfigHandler.weapondamage.spartanTwoHandedBuffLevel2 : ForgeConfigHandler.weapondamage.spartanTwoHandedBuffLevel1;
        if (buff <= 0.0F) return;

        List<String> tooltip = event.getToolTip();
        String line = TextFormatting.ITALIC + "  " + I18n.format("eaglemixins.tooltip.two_handed_buff", Math.round(buff * 100.0F));
        tooltip.add(findInsertionIndex(tooltip, level), line);
    }

    private static int findTwoHandedLevel(List<WeaponProperty> props) {
        for (WeaponProperty prop : props) {
            if (prop.getLevel() > 0) return prop.getLevel();
        }
        return 0;
    }

    // Best-effort: insert right after Spartan Weaponry's own "Two-Handed" title+description lines; append at the end otherwise.
    private static int findInsertionIndex(List<String> tooltip, int level) {
        String titleText = I18n.format("tooltip.spartanweaponry:two_handed", level >= 2 ? "II" : "I");
        for (int i = 0; i < tooltip.size(); i++) {
            if (tooltip.get(i).contains(titleText)) {
                return Math.min(i + 2, tooltip.size());
            }
        }
        return tooltip.size();
    }
}