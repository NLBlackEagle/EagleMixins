package eaglemixins.mixin.rlartifacts;

import artifacts.common.entity.EntityMimic;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(targets = "artifacts.common.entity.EntityMimic$MimicEventHandler")
public class MimicEventHandlerMixin {

    @Inject(
            method = "onRightClick",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    private static void onRightClick(PlayerInteractEvent.RightClickBlock event, CallbackInfo ci) {
        if(event.getUseBlock() == Event.Result.DENY ||
                event.getWorld().isRemote ||
                event.getEntityPlayer() == null ||
                ForgeConfigHandler.server.undergroundMimicChance <= 0F) return;
        BlockPos pos = event.getPos();
        if (pos.getY() > 25) {
            return;
        }
        World world = event.getWorld();
        TileEntity tile = world.getTileEntity(pos);
        Block block = world.getBlockState(pos).getBlock();
        EntityPlayer player = event.getEntityPlayer();
        if(tile instanceof TileEntityChest && block instanceof BlockChest && !player.isSpectator()) {
            if(!Arrays.asList(ForgeConfigHandler.server.undergroundMimicDimensions).contains(event.getWorld().provider.getDimension())) return;
            if(world.getBlockState(pos.up()).doesSideBlockChestOpening(world, pos.up(), EnumFacing.DOWN)) return;
            if(((ILootContainer)tile).getLootTable() != null) {
                ((TileEntityChest) tile).fillWithLoot(player);
                if(world.rand.nextDouble() <= ForgeConfigHandler.server.undergroundMimicChance) {
                    event.setCanceled(true);
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                    EntityMimic mimic = new EntityMimic(world);
                    mimic.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    mimic.enablePersistence();
                    mimic.setAwakeWithTarget(player);
                    world.spawnEntity(mimic);
                }
            }
        }
        ci.cancel();
    }

    @Inject(
            method = "onBlockBreak",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    private static void onBlockBreak(BlockEvent.BreakEvent event, CallbackInfo ci) {
        if(event.getWorld().isRemote || event.getPlayer() == null || ForgeConfigHandler.server.undergroundMimicChance <= 0F) return;
        BlockPos pos = event.getPos();
        if (pos.getY() > 25) {
            return;
        }
        World world = event.getWorld();
        TileEntity tile = world.getTileEntity(pos);
        Block block = world.getBlockState(pos).getBlock();
        EntityPlayer player = event.getPlayer();
        if(tile instanceof TileEntityChest && block instanceof BlockChest && !player.isSpectator()) {
            if(!Arrays.asList(ForgeConfigHandler.server.undergroundMimicDimensions).contains(event.getWorld().provider.getDimension())) return;
            if(((ILootContainer)tile).getLootTable() != null) {
                ((TileEntityChest) tile).fillWithLoot(player);
                if(world.rand.nextDouble() <= ForgeConfigHandler.server.undergroundMimicChance) {
                    event.setCanceled(true);
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                    EntityMimic mimic = new EntityMimic(world);
                    mimic.setPosition(pos.getX()+0.5, pos.getY(), pos.getZ()+0.5);
                    mimic.enablePersistence();
                    mimic.setAwakeWithTarget(player);
                    world.spawnEntity(mimic);
                }
            }
        }
        ci.cancel();
    }
}
