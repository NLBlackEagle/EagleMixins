package eaglemixins.teleport;

import eaglemixins.EagleMixins;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.network.PacketStopTeleportOverlay;
import eaglemixins.potion.PotionTeleportationSickness;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class TeleportService {

    private static final Queue<EntityPlayer> TELEPORT_QUEUE = new ConcurrentLinkedQueue<>();

    private TeleportService() {}

    public static void enqueue(EntityPlayer player) {
        TELEPORT_QUEUE.add(player);
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        while (!TELEPORT_QUEUE.isEmpty()) {
            EntityPlayer player = TELEPORT_QUEUE.poll();
            if (player == null || player.world == null) continue;

            NBTTagCompound tag = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            int linkId = tag.getInteger("linkId");
            TeleportData data = TeleportRegistry.get(linkId);
            if (data == null || data.receiver == null) continue;

            BlockPos finalPos = data.receiver.add(0, 3, 3);
            MinecraftServer server = player.getServer();
            if (server != null) {
                server.addScheduledTask(() -> {
                    player.setPositionAndUpdate(finalPos.getX() + 0.5, finalPos.getY(), finalPos.getZ() + 0.5);
                    player.addPotionEffect(new PotionEffect(PotionTeleportationSickness.INSTANCE, 200, 0));
                });
            }

            if (player instanceof EntityPlayerMP) {
                EagleMixins.NETWORK.sendTo(new PacketStopTeleportOverlay(), (EntityPlayerMP) player);
            }

            tag.removeTag("justTeleported");
            tag.removeTag("linkId");
            player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
        }
    }

    public static boolean onePercent(java.util.Random rand) {
        return rand.nextInt(100) < ForgeConfigHandler.server.teleportation_chance;
    }
}
