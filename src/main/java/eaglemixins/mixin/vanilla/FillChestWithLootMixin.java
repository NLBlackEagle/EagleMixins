package eaglemixins.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import eaglemixins.util.LootGenerationContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TileEntityLockableLoot.class)
public abstract class FillChestWithLootMixin {

    @Shadow protected ResourceLocation lootTable;

    @WrapMethod(method = "fillWithLoot")
    private void eaglemixins_pushTopLootTable(EntityPlayer player, Operation<Void> original) {
        if(this.lootTable != null) {
            LootGenerationContext.push(this.lootTable);
            original.call(player);
            LootGenerationContext.pop();
        } else
            original.call(player);
    }
}