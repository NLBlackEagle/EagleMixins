package eaglemixins.handlers;

import eaglemixins.EagleMixins;
import eaglemixins.radiation.NbtStackRadiation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class ItemRadCapAttacher {
    private static final ResourceLocation KEY = new ResourceLocation(EagleMixins.MODID, "nbt_radiation");

    @SubscribeEvent
    public static void onAttach(AttachCapabilitiesEvent<ItemStack> e) {
        // Attach to all stacks; provider only "exists" when the NBT key is present.
        e.addCapability(KEY, new NbtStackRadiation(e.getObject()));
    }
}
