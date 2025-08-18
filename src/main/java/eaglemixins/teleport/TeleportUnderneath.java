package eaglemixins.teleport;

import eaglemixins.EagleMixins;
import eaglemixins.network.PacketStartTeleportOverlay;
import eaglemixins.network.PacketStopTeleportOverlay;
import eaglemixins.potion.PotionTeleportationSickness;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Random;

public final class TeleportUnderneath {
    private static final Logger LOGGER = LogManager.getLogger("EagleMixins");

    public static final int GLITCH_DIM = 3;
    public static final int OVERWORLD_DIM = 0;

    private TeleportUnderneath() {}

    public static void triggerGlitch(EntityPlayer player) {
        if (!DimensionManager.isDimensionRegistered(GLITCH_DIM) || !DimensionManager.isDimensionRegistered(OVERWORLD_DIM)) {
            LOGGER.error("[EagleMixins] Glitch or Overworld dimension not registered.");
            return;
        }

        WorldServer glitchWorld = DimensionManager.getWorld(GLITCH_DIM);
        if (glitchWorld == null) {
            DimensionManager.initDimension(GLITCH_DIM);
            glitchWorld = DimensionManager.getWorld(GLITCH_DIM);
        }

        WorldServer overworld = DimensionManager.getWorld(OVERWORLD_DIM);
        if (overworld == null) {
            DimensionManager.initDimension(OVERWORLD_DIM);
            overworld = DimensionManager.getWorld(OVERWORLD_DIM);
        }

        if (glitchWorld == null || overworld == null) {
            LOGGER.error("[EagleMixins] Could not access glitch or overworld dimension after init.");
            return;
        }

        if (!player.world.isRemote && player instanceof EntityPlayerMP) {
            EntityPlayerMP mp = (EntityPlayerMP) player;
            EagleMixins.NETWORK.sendTo(new PacketStartTeleportOverlay(true), mp);

            Objects.requireNonNull(mp.getServer()).getPlayerList().transferPlayerToDimension(mp, GLITCH_DIM, new NullPortalTeleporter(glitchWorld));

            boolean firstTime = !getPersist(mp).getBoolean("glitchDone");
            getPersist(mp).setBoolean("glitchDone", true);

            long stayTicks = firstTime ? 140 : 140 + player.world.rand.nextInt(260);
            getPersist(mp).setLong("glitchEndTime", player.world.getTotalWorldTime() + stayTicks);

            mp.capabilities.disableDamage = true;
            mp.sendPlayerAbilities();
        }
    }

    /** Called from player tick: returns the player to stored finalPos and removes overlay/immunity. */
    public static void tryReturn(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) return;
        EntityPlayerMP mp = (EntityPlayerMP) player;

        NBTTagCompound p = getPersist(mp);
        if (!p.hasKey("glitchEndTime")) return;
        if (player.world.getTotalWorldTime() < p.getLong("glitchEndTime")) return;

        int originDim = p.getInteger("glitchOriginDim");
        if (!DimensionManager.isDimensionRegistered(originDim)) {
            LOGGER.error("[EagleMixins] Glitch return failed: dimension {} not registered.", originDim);
            return;
        }

        double x = p.getDouble("glitchReturnX");
        double y = p.getDouble("glitchReturnY");
        double z = p.getDouble("glitchReturnZ");

        WorldServer target = DimensionManager.getWorld(originDim);
        if (target == null) return;

        mp.capabilities.disableDamage = false;
        mp.sendPlayerAbilities();

        EagleMixins.NETWORK.sendTo(new PacketStartTeleportOverlay(true), mp);

        Objects.requireNonNull(mp.getServer()).getPlayerList().transferPlayerToDimension(mp, originDim, new NullPortalTeleporter(target));
        MinecraftServer server = mp.getServer();
        if (server != null) {
            server.addScheduledTask(() -> {
                mp.setPositionAndUpdate(x + 0.5, y, z + 0.5);
                mp.addPotionEffect(new PotionEffect(PotionTeleportationSickness.INSTANCE, 200, 0));
            });
        }

        EagleMixins.NETWORK.sendTo(new PacketStopTeleportOverlay(), mp);

        p.removeTag("glitchEndTime");
        p.removeTag("glitchOriginDim");
        p.removeTag("glitchReturnX");
        p.removeTag("glitchReturnY");
        p.removeTag("glitchReturnZ");
        setPersist(mp, p);
    }

    public static BlockPos findSafeGlitchPosition(WorldServer world) {
        Random rand = new Random();
        for (int attempt = 0; attempt < 100; attempt++) {
            int x = (rand.nextBoolean() ? 1 : -1) * (5000 + rand.nextInt(10000));
            int z = (rand.nextBoolean() ? 1 : -1) * (5000 + rand.nextInt(10000));

            for (int y = 150; y > 10; y--) {
                BlockPos floor = new BlockPos(x, y, z);
                BlockPos a1 = floor.up();
                BlockPos a2 = floor.up(2);

                if (world.getBlockState(floor).isOpaqueCube() &&
                        (world.isAirBlock(a1) || world.getBlockState(a1).getMaterial().isLiquid()) &&
                        (world.isAirBlock(a2) || world.getBlockState(a2).getMaterial().isLiquid())) {
                    return a1;
                }
            }

            BlockPos top = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
            if (top != null) return top.up();
        }
        return new BlockPos(0, 250, 0);
    }

    private static NBTTagCompound getPersist(EntityPlayer player) {
        return player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
    }

    private static void setPersist(EntityPlayer player, NBTTagCompound tag) {
        player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
    }

    public static final class NullPortalTeleporter extends net.minecraft.world.Teleporter {
        public NullPortalTeleporter(WorldServer worldIn) { super(worldIn); }

        @Override public void placeInPortal(Entity entity, float rotationYaw) {
            BlockPos pos = TeleportUnderneath.findSafeGlitchPosition(this.world);
            entity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            entity.motionX = entity.motionY = entity.motionZ = 0.0;
        }
        @Override public boolean placeInExistingPortal(Entity entity, float rotationYaw) { return true; }
        @Override public boolean makePortal(Entity entity) { return true; }
        @Override public void removeStalePortalLocations(long worldTime) {}
    }
}
