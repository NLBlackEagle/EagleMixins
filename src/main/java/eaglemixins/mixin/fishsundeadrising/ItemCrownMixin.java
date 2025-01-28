package eaglemixins.mixin.fishsundeadrising;

import com.Fishmod.mod_LavaCow.item.ItemCrown;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemCrown.class)
public class ItemCrownMixin extends Item {
    @Redirect(
            method = "onItemUse",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/BiomeDictionary;hasType(Lnet/minecraft/world/biome/Biome;Lnet/minecraftforge/common/BiomeDictionary$Type;)Z"),
            remap = false
    )
    public boolean eagleMixins_furItemCrown_hasType(Biome biome, BiomeDictionary.Type type) {
        //Disable original handling
        return true;
    }

    @Inject(
            method = "onItemUse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;"),
            cancellable = true
    )
    public void eagleMixins_furItemCrown_getTileEntity(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<EnumActionResult> cir) {
        //Check both old (biome type) and new handling (biome name), return if biome doesnt fit either

        //TODO: otg get biome instead of using this clientside function
        String biomeName = worldIn.getBiome(player.getPosition()).getBiomeName();
        boolean newHandling = biomeName.contains("Desert") || biomeName.contains("Dune") || biomeName.contains("Wasteland");

        boolean oldHandling = BiomeDictionary.hasType(worldIn.getBiome(pos), BiomeDictionary.Type.HOT) && BiomeDictionary.hasType(worldIn.getBiome(pos), BiomeDictionary.Type.DRY) && BiomeDictionary.hasType(worldIn.getBiome(pos), BiomeDictionary.Type.SANDY);

        if (!newHandling && !oldHandling)
            cir.setReturnValue(super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ));
    }
}