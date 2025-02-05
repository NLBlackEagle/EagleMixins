package eaglemixins.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;

public class DisarmingUtility {
    public static boolean isAllowedToDisarm(ItemStack stack, EntityLivingBase entityDisarmed, EntityLivingBase entityDisarming){
        //Players are allowed to get their weapons disarmed
        if (entityDisarmed instanceof EntityPlayer) return true;

        //Don't worry about items with max stack size >1 (assumption: not gear)
        if(stack.getMaxStackSize() != 1) return true;

        ResourceLocation resourceLocation = stack.getItem().getRegistryName();
        if (resourceLocation == null) return true;
        String itemId = resourceLocation.getPath();
        String itemModId = resourceLocation.getNamespace();
        //Don't drop anything if its living/sentient/dragonbone
        return !(itemModId.equals(Ref.SRPMODID) || itemId.contains("dragonbone"));
    }
}
