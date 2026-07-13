package eaglemixins.mixin.sereneseasons;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockReed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import sereneseasons.api.SSBlocks;
import sereneseasons.config.FertilityConfig;
import sereneseasons.handler.season.SeasonalCropGrowthHandler;

@Mixin(value = SeasonalCropGrowthHandler.class, remap = false)
public abstract class SeasonalCropHandlerMixins
{
    /**
     * @author NLBlackEagle
     * @reason Seasons does not support metadata, now it does.
     */
    @Overwrite
    @SubscribeEvent
    public void onCropGrowth(BlockEvent.CropGrowEvent event)
    {
        IBlockState state = event.getState();
        Block plant = state.getBlock();
        int meta = plant.getMetaFromState(state);

        boolean isFertile = FertilityMixins.isCropFertileMeta(
                plant.getRegistryName().toString(), meta, event.getWorld(), event.getPos());

        if (FertilityConfig.general_category.seasonal_crops && !isFertile && !isGreenhouseGlassAboveBlock(event.getWorld(), event.getPos()))
        {
            if (FertilityConfig.general_category.crops_break && !(plant instanceof BlockGrass) && !(plant instanceof BlockReed))
            {
                event.getWorld().destroyBlock(event.getPos(), true);
            }
            else
            {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    /**
     * @author NLBlackEagle
     * @reason Seasons does not support metadata, now it does.
     */
    @Overwrite
    @SubscribeEvent
    public void onApplyBonemeal(BonemealEvent event)
    {
        IBlockState state = event.getBlock();
        Block plant = state.getBlock();
        int meta = plant.getMetaFromState(state);

        boolean isFertile = FertilityMixins.isCropFertileMeta(
                plant.getRegistryName().toString(), meta, event.getWorld(), event.getPos());

        if (FertilityConfig.general_category.seasonal_crops && !isFertile && !isGreenhouseGlassAboveBlock(event.getWorld(), event.getPos()))
        {
            if (FertilityConfig.general_category.crops_break && !(plant instanceof BlockGrass) && !(plant instanceof BlockReed))
            {
                event.getWorld().destroyBlock(event.getPos(), true);
            }

            event.setCanceled(true);
        }
    }

    // Duplicated from the original private method since @Overwrite methods can't call
    // the original private helper directly once it's shadowed out; keep behaviour identical.
    private boolean isGreenhouseGlassAboveBlock(World world, BlockPos cropPos)
    {
        for (int i = 0; i < FertilityConfig.general_category.greenhouse_glass_max_height; i++)
        {
            if (world.getBlockState(cropPos.add(0, i + 1, 0)).getBlock().equals(SSBlocks.greenhouse_glass))
            {
                return true;
            }
        }
        return false;
    }
}
