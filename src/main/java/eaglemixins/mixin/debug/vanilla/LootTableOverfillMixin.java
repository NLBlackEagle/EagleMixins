package eaglemixins.mixin.debug.vanilla;

import eaglemixins.debug.LootDebug;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootTable.class)
public abstract class LootTableOverfillMixin {
    private static final Logger LOG = LogManager.getLogger("OverfillLogger");

    @Inject(
            method = "fillInventory(Lnet/minecraft/inventory/IInventory;Ljava/util/Random;Lnet/minecraft/world/storage/loot/LootContext;)V",
            at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;)V", remap = false)
            // If this fails at runtime, try the varargs overload:
            // at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V")
    )
    private void eagle$logOverfill(IInventory inv, java.util.Random rand, LootContext ctx, CallbackInfo ci) {
        ResourceLocation id = LootDebug.LT_IDS.get((LootTable)(Object)this);
        String where = inv.getClass().getName();
        String posLine = "";
        if (inv instanceof TileEntity) {
            TileEntity te = (TileEntity) inv;
            BlockPos p = te.getPos();
            World w = te.getWorld();
            int dim = (w != null && w.provider != null) ? w.provider.getDimension() : 0;
            posLine = " at " + p + " dim=" + dim;
        }
        int capacity = inv.getSizeInventory();
        LOG.warn("[Overfill] table={} capacity={} inv={}{}", id, capacity, where, posLine);
    }
}