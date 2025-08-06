package eaglemixins.handlers;

import com.anthonyhilyard.legendarytooltips.render.TooltipDecor;
import eaglemixins.mixin.legendarytooltips.LegendaryTooltipsAccessor;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraft.item.ItemStack;
import java.util.Arrays;

@EventBusSubscriber
public class TooltipColorDebugger {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderTooltipColorEvent(RenderTooltipEvent.Color event) {
        ItemStack stack = event.getStack();
        Integer start = event.getBorderStart();
        Integer end = event.getBorderEnd();
        Integer bg = event.getBackground();

        System.out.println("[TooltipCrashDebugger] ---- TooltipColorEvent START ----");
        System.out.println("[TooltipCrashDebugger] Stack: " + (stack == null ? "null" : stack.toString()));
        System.out.println("[TooltipCrashDebugger] Border Start: " + start);
        System.out.println("[TooltipCrashDebugger] Border End: " + end);
        System.out.println("[TooltipCrashDebugger] Background: " + bg);

        if (stack == null || stack.isEmpty()) {
            System.out.println("[TooltipCrashDebugger] ItemStack is null or empty.");
        }

        if (start == null || end == null || bg == null) {
            System.out.println("[TooltipCrashDebugger] One or more default colors are null.");
        }

        System.out.println("[TooltipCrashDebugger] ---- TooltipColorEvent END ----");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onTooltipColorEvent(RenderTooltipEvent.Color event) {
        try {
            ItemStack stack = event.getStack();
            Integer[] defaults = new Integer[] {
                    event.getBorderStart(),
                    event.getBorderEnd(),
                    event.getBackground()
            };

            System.out.println("[TooltipColorDebugger] Stack: " + stack);
            System.out.println("[TooltipColorDebugger] Defaults: " + Arrays.toString(defaults));

            // Call original method
            Integer[] borderColors = LegendaryTooltipsAccessor.callItemFrameColors(stack, defaults);

            System.out.println("[TooltipColorDebugger] itemFrameColors returned: " + Arrays.toString(borderColors));

            TooltipDecor.setCurrentTooltipBorderStart(borderColors[0]);
            TooltipDecor.setCurrentTooltipBorderEnd(borderColors[1]);

            boolean comparison = false;
            if (Loader.isModLoaded("equipmentcompare")) {
                try {
                    comparison = (Boolean) Class
                            .forName("com.anthonyhilyard.legendarytooltips.compat.EquipmentCompareHandler")
                            .getMethod("isComparisonEvent", net.minecraftforge.fml.common.eventhandler.Event.class)
                            .invoke(null, event);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (comparison) {
                event.setBorderStart(0);
                event.setBorderEnd(0);
            } else {
                event.setBorderStart(borderColors[0]);
                event.setBorderEnd(borderColors[1]);
            }

            event.setBackground(borderColors[2]);
        } catch (Exception ex) {
            System.out.println("[TooltipColorDebugger] Caught exception:");
            ex.printStackTrace();
        }
    }
}
