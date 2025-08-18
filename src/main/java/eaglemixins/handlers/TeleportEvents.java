package eaglemixins.handlers;

import eaglemixins.EagleMixins;
import eaglemixins.network.PacketStartTeleportOverlay;
import eaglemixins.potion.PotionTeleportationSickness;
import eaglemixins.teleport.*;
import eaglemixins.teleport.TeleportUnderneath;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;

public class TeleportEvents {

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        TeleportService.onServerTick(e);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        EntityPlayer p = e.player;
        if (p.world.isRemote || !(p instanceof EntityPlayerMP)) return;

        NBTTagCompound tag = p.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        if (p.dimension == TeleportUnderneath.GLITCH_DIM && tag.hasKey("glitchEndTime")) {
            TeleportUnderneath.tryReturn(p);
        }
    }

    @SubscribeEvent
    public void onPlayerTeleport(PlayerEvent e) {
        if (!(e.getEntityPlayer() instanceof EntityPlayerMP)) return;

        EntityPlayerMP player = (EntityPlayerMP) e.getEntityPlayer();
        World world = player.world;
        TeleportRegistry.ensureInit();

        if (player.getActivePotionEffect(PotionTeleportationSickness.INSTANCE) != null) return;
        if (!isOnEndPortal(world, player.getPosition().down())) return;

        for (Map.Entry<Integer, TeleportData> entry : TeleportRegistrySnapshot.view().entrySet()) {
            final int linkId = entry.getKey();
            final TeleportData data = entry.getValue();

            // Near sender? (sender defaults to approx in registry until discovered)
            if (data.sender != null && player.getDistanceSq(data.sender) <= 36) {
                BlockPos returnPos = (data.receiver != null ? data.receiver : TeleportData.senderApprox(linkId));

                if (TeleportService.onePercent(world.rand) && data.receiver != null) {
                    rememberGlitchReturn(player, returnPos);
                    EagleMixins.NETWORK.sendTo(new PacketStartTeleportOverlay(true), player);
                    player.addStat(ModStats.GLITCH_COUNT);
                    TeleportUnderneath.triggerGlitch(player);
                    return;
                }

                if (data.receiver != null) {
                    teleportLater(player,
                            data.receiver.getX() + 0.5,
                            data.receiver.getY() + 2,
                            data.receiver.getZ() + 3.5,
                            true);
                } else if (data.tempReceiver != null) {
                    EagleMixins.NETWORK.sendTo(new PacketStartTeleportOverlay(false), player);
                    BlockPos safe = world.getTopSolidOrLiquidBlock(new BlockPos(data.tempReceiver.getX(), 0, data.tempReceiver.getZ()));
                    teleportLater(player, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5, false);
                } else {
                    continue;
                }

                markJustTeleported(player, linkId);
                return;
            }

            // Near receiver? (only when exact receiver known, matches original behavior)
            if (data.receiver != null && player.getDistanceSq(data.receiver) <= 36) {
                BlockPos returnPos = (data.sender != null ? data.sender : TeleportData.receiverApprox(linkId));

                if (TeleportService.onePercent(world.rand) && data.sender != null) {
                    rememberGlitchReturn(player, returnPos);
                    EagleMixins.NETWORK.sendTo(new PacketStartTeleportOverlay(true), player);
                    player.addStat(ModStats.GLITCH_COUNT);
                    TeleportUnderneath.triggerGlitch(player);
                    return;
                }

                if (data.sender != null) {
                    teleportLater(player,
                            data.sender.getX() + 0.5,
                            data.sender.getY() + 2,
                            data.sender.getZ() + 3.5,
                            true);
                } else {
                    EagleMixins.NETWORK.sendTo(new PacketStartTeleportOverlay(false), player);
                    BlockPos approx = TeleportData.senderApprox(linkId);
                    BlockPos safe = world.getTopSolidOrLiquidBlock(new BlockPos(approx.getX(), 0, approx.getZ()));
                    teleportLater(player, safe.getX() + 0.5, safe.getY() + 0.5, safe.getZ() + 0.5, false);
                }

                markJustTeleported(player, linkId);
                return;
            }
        }
    }

    private static boolean isOnEndPortal(World world, BlockPos basePos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos checkPos = basePos.add(dx, 0, dz);
                Block block = world.getBlockState(checkPos).getBlock();
                if (block instanceof BlockEndPortal) return true;
            }
        }
        Block block = world.getBlockState(basePos.down()).getBlock();
        return block instanceof BlockEndPortal;
    }

    private static void teleportLater(EntityPlayerMP p, double x, double y, double z, boolean giveSickness) {
        MinecraftServer server = p.getServer();
        if (server != null) {
            server.addScheduledTask(() -> {
                p.setPositionAndUpdate(x, y, z);
                if (giveSickness) {
                    p.addPotionEffect(new PotionEffect(PotionTeleportationSickness.INSTANCE, 200, 0));
                }
            });
        }
    }

    private static void markJustTeleported(EntityPlayer p, int linkId) {
        NBTTagCompound tag = p.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        tag.setBoolean("justTeleported", true);
        tag.setInteger("linkId", linkId);
        p.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
    }

    private static void rememberGlitchReturn(EntityPlayer p, BlockPos returnPos) {
        NBTTagCompound t = p.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        t.setDouble("glitchReturnX", returnPos.getX() + 0.5);
        t.setDouble("glitchReturnY", returnPos.getY() + 2);
        t.setDouble("glitchReturnZ", returnPos.getZ() + 3.5);
        t.setInteger("glitchOriginDim", p.dimension);
        p.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, t);
    }

    private static final class TeleportRegistrySnapshot {
        static java.util.Map<Integer, TeleportData> view() {
            java.util.Map<Integer, TeleportData> copy = new java.util.HashMap<>();
            for (int id : TeleportData.ALL_IDS) {
                TeleportData d = TeleportRegistry.get(id);
                if (d != null) copy.put(id, d);
            }
            return copy;
        }
    }
}
