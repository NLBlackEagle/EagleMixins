package eaglemixins.mixin.nuclearcraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import eaglemixins.config.ForgeConfigHandler;
import nc.radiation.RadiationHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RadiationHelper.class)
public abstract class RadiationHelper_IrradiatedLootTables {
    @ModifyExpressionValue(
            method = "transferRadiationFromProviderToChunkBuffer",
            at = @At(value = "INVOKE", target = "Lnc/radiation/RadiationHelper;getTileInventory(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;Lnet/minecraft/util/EnumFacing;)Lnet/minecraftforge/items/IItemHandler;")
    )
    private static IItemHandler eagleMixins_allowRadiationOnLootTables(IItemHandler original, @Local(name = "rawRadiation") LocalDoubleRef rawRadiation){
        if(!(original instanceof ILootContainer)) return original;
        ResourceLocation lootTable = ((ILootContainer) original).getLootTable();
        if(lootTable == null) return original;

        rawRadiation.set(rawRadiation.get() + ForgeConfigHandler.server.lootTableRadiation.getOrDefault(lootTable.toString(), 0.0));
        return null; //don't check the actual contents, otherwise the loot would be generated
    }
}
