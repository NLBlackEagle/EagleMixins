package eaglemixins.items;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/** Plain material item with a single gray italic lore line from "eaglemixins.<name>.tooltip". */
public class ItemLore extends Item {
    private final String tooltipKey;

    public ItemLore(String name) {
        this.tooltipKey = "eaglemixins." + name + ".tooltip";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + I18n.format(tooltipKey));
    }
}
