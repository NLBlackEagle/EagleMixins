package eaglemixins.compat;

import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import tschipp.forgottenitems.util.FIHelper;

public class ForgottenItemsUtil {

    public static void remapRecipeCoreItems() {
        FIHelper.OUTPUTS_CORES.forEach(recipeMap -> {
            // Single entry map stored in an array
            if(recipeMap != null) {
                for(Item outputItem : recipeMap.keySet()) {
                    ResourceLocation itemOverride = ForgeConfigHandler.server.forgottenItemsRecipeCoreItems.get(outputItem.getRegistryName());
                    if(itemOverride != null) {
                        Item coreItem = ForgeRegistries.ITEMS.getValue(itemOverride);
                        if(coreItem != null) {
                            recipeMap.put(outputItem, coreItem);
                        }
                    }
                }
            }
        });
    }
}
