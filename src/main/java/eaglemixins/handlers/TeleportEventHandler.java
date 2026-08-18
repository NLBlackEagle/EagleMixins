package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.init.ModStats;
import eaglemixins.network.PacketHandler;
import eaglemixins.network.PacketStartTeleportOverlay;
import eaglemixins.potion.PotionTeleportationSickness;
import eaglemixins.teleport.*;
import eaglemixins.teleport.TeleportData;
import eaglemixins.teleport.TeleportDataHandler;
import eaglemixins.teleport.TeleportNBTKeys;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;

public class TeleportEventHandler {

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        TeleportService.teleportPlayersToNewTeleporter(e);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.side.isClient() || e.phase != TickEvent.Phase.END) return;
        if (!(e.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) e.player;
        World world = player.world;

        // Return player to overworld (if they got glitched into underneath)
        TeleportUnderneath.returnToTeleporter(player);

        if (player.isPotionActive(PotionTeleportationSickness.INSTANCE)) return;
        // teleporter only works if the portal is lit
        if (!isNearEndPortal(world, player.getPosition().down())) return;

        //find nearest registered portal
        for (Map.Entry<Integer, TeleportData> entry : TeleportDataHandler.get(world).getRegistry().entrySet()) {
            int linkId = entry.getKey();
            TeleportData data = entry.getValue();
            for (boolean isSenderToReceiver : new boolean[]{true, false}) { // S -> R and R -> S
                boolean thisPosDiscovered = data.isDiscovered(isSenderToReceiver);
                boolean otherPosDiscovered = data.isDiscovered(!isSenderToReceiver);
                BlockPos thisPos = data.getBlockPos(isSenderToReceiver);
                BlockPos otherPos = data.getBlockPos(!isSenderToReceiver);

                if (thisPosDiscovered && player.getDistanceSq(thisPos) <= 36) {

                    if(!otherPosDiscovered) // This is to be able to re-teleport the player to the correct spot upon arrival
                        setUsingTeleportLinkID(player, linkId, isSenderToReceiver);

                    if(attemptGlitchedTeleport(player, otherPos)) return;

                    PacketHandler.sendTo(new PacketStartTeleportOverlay(false), player);
                    if (otherPosDiscovered) {
                        // Normal teleport, both generated
                        TeleportService.teleportWithSickness(player,
                                otherPos.getX(),
                                otherPos.getY() + 2,
                                otherPos.getZ() + 3
                        );
                    } else {
                        // Teleporting to not yet generated teleporter structure
                        BlockPos safe = world.getTopSolidOrLiquidBlock(new BlockPos(otherPos.getX(), 0, otherPos.getZ()));
                        TeleportService.teleportWithSickness(player,
                                safe.getX(),
                                safe.getY(),
                                safe.getZ()
                        );
                    }

                    return;
                }
            }
        }
    }

    private static boolean attemptGlitchedTeleport(EntityPlayerMP player, BlockPos returnPos){
        if (player.getRNG().nextFloat() < ForgeConfigHandler.teleporter.teleportationChance) {
            rememberGlitchReturn(player, returnPos);
            PacketHandler.sendTo(new PacketStartTeleportOverlay(true), player);
            player.addStat(ModStats.GLITCH_COUNT);
            TeleportUnderneath.teleportToUnderneath(player);
            return true;
        }
        return false;
    }

    private static boolean isNearEndPortal(World world, BlockPos basePos) {
        // Any End Portal in 9x9 around player ?
        for (BlockPos checkPos : BlockPos.MutableBlockPos.getAllInBox(
                basePos.getX() - 1, basePos.getY(), basePos.getZ() - 1,
                basePos.getX() + 1, basePos.getY(), basePos.getZ() + 1)
        ) {
            if (world.getBlockState(checkPos).getBlock() instanceof BlockEndPortal)
                return true;
        }
        // End Portal right below?
        return world.getBlockState(basePos.down()).getBlock() instanceof BlockEndPortal;
    }

    private static void setUsingTeleportLinkID(EntityPlayer player, int linkId, boolean toReceiver) {
        NBTTagCompound persistedData = TeleportService.getPlayerPersistedData(player);
        persistedData.setInteger(TeleportNBTKeys.PLAYER_CURR_LINKID, linkId);
        persistedData.setBoolean(TeleportNBTKeys.PLAYER_CURR_DIRECTION, toReceiver);
    }

    private static void rememberGlitchReturn(EntityPlayer player, BlockPos returnPos) {
        NBTTagCompound persistedData = TeleportService.getPlayerPersistedData(player);
        persistedData.setDouble(TeleportNBTKeys.RETURN_X, returnPos.getX());
        persistedData.setDouble(TeleportNBTKeys.RETURN_Y, returnPos.getY() + 2);
        persistedData.setDouble(TeleportNBTKeys.RETURN_Z, returnPos.getZ() + 3);
        persistedData.setInteger(TeleportNBTKeys.RETURN_DIM, player.dimension);
    }
}
