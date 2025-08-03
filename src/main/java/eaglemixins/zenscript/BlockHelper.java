package eaglemixins.zenscript;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.block.Block;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import surreal.contentcreator.proxy.CommonProxy;

@ZenRegister
@ZenClass("mods.eaglemixins.BlockHelper")
public class BlockHelper {
    @ZenMethod
    public static void setBlockHardness(String name, Float hardness) {
        for (Block block : CommonProxy.BLOCKS) {
            if (block.getRegistryName().toString().equals(name)) {
                block.setHardness(hardness);
            }
        }
    }
}
