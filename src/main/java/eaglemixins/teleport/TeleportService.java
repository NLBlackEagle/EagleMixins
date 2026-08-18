package eaglemixins.teleport;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.network.PacketHandler;
import eaglemixins.network.PacketStopTeleportOverlay;
import eaglemixins.potion.PotionTeleportationSickness;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public final class TeleportService {

    private static final List<EntityPlayerMP> RE_TELEPORT_QUEUE = new ArrayList<>();

    private TeleportService() {}

    public static void enqueueToReTeleport(EntityPlayerMP player) {
        RE_TELEPORT_QUEUE.add(player);
    }

    // This is only used to teleport players AGAIN right after the receiver structure generated due to the first teleport to the receiver
    public static void teleportPlayersToNewTeleporter(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (RE_TELEPORT_QUEUE.isEmpty()) return;

        for (EntityPlayerMP player : RE_TELEPORT_QUEUE) {
            NBTTagCompound persistedData = TeleportService.getPlayerPersistedData(player);
            if(!persistedData.hasKey(TeleportNBTKeys.PLAYER_CURR_LINKID)) continue; // player generated the chunk differently, e.g. by exploring
            int linkId = persistedData.getInteger(TeleportNBTKeys.PLAYER_CURR_LINKID);
            boolean toReceiver = persistedData.getBoolean(TeleportNBTKeys.PLAYER_CURR_DIRECTION);

            // cleanup
            persistedData.removeTag(TeleportNBTKeys.PLAYER_CURR_LINKID);
            persistedData.removeTag(TeleportNBTKeys.PLAYER_CURR_DIRECTION);

            TeleportData data = TeleportDataHandler.get(player.world).getTeleportData(linkId);
            if (data == null || !data.isDiscovered(!toReceiver)) continue;

            BlockPos targetPos = data.getBlockPos(!toReceiver);
            teleportWithSickness(player,
                    targetPos.getX(),
                    targetPos.getY() + 2,
                    targetPos.getZ() + 3
            );
        }

        RE_TELEPORT_QUEUE.clear();
    }

    public static void teleportWithSickness(EntityPlayerMP player, double x, double y, double z){
        MinecraftServer server = player.getServer();
        if (server != null) {
            server.addScheduledTask(() -> {
                player.setPositionAndUpdate(x + 0.5, y, z + 0.5);
                if(ForgeConfigHandler.teleporter.sicknessDuration > 0)
                    player.addPotionEffect(new PotionEffect(PotionTeleportationSickness.INSTANCE, 20 * ForgeConfigHandler.teleporter.sicknessDuration, 0));
                PacketHandler.sendTo(new PacketStopTeleportOverlay(), player);
            });
        }
    }

    public static NBTTagCompound getPlayerPersistedData(EntityPlayer player){
        return player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG); // Not having this in a separate sub-compound isn't perfect but i just gave them em$ prefixes now
    }
}
