package eaglemixins.teleport;

import eaglemixins.EagleMixins;
import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.network.PacketHandler;
import eaglemixins.network.PacketStartTeleportOverlay;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public final class TeleportUnderneath {

    public static final int GLITCH_DIM = 3;

    private TeleportUnderneath() {}

    public static void teleportToUnderneath(EntityPlayer player) {
        if (!DimensionManager.isDimensionRegistered(GLITCH_DIM)) {
            EagleMixins.LOGGER.error("Glitch dimension not registered.");
            return;
        }

        WorldServer glitchWorld = DimensionManager.getWorld(GLITCH_DIM);
        if (glitchWorld == null) {
            DimensionManager.initDimension(GLITCH_DIM);
            glitchWorld = DimensionManager.getWorld(GLITCH_DIM);
        }

        if (glitchWorld == null) {
            EagleMixins.LOGGER.error("Could not access glitch dimension after init.");
            return;
        }

        if (!player.world.isRemote && player instanceof EntityPlayerMP && player.getServer() != null) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            PacketHandler.sendTo(new PacketStartTeleportOverlay(true), playerMP);

            playerMP.getServer().getPlayerList().transferPlayerToDimension(playerMP, GLITCH_DIM, new GlitchedPortalTeleporter(glitchWorld));

            NBTTagCompound persistedData = TeleportService.getPlayerPersistedData(player);
            boolean isFirstTime = !persistedData.getBoolean(TeleportNBTKeys.NOT_FIRST_TIME);
            persistedData.setBoolean(TeleportNBTKeys.NOT_FIRST_TIME, true);

            long stayTicks = ForgeConfigHandler.teleporter.glitchDurationMin;
            if(!isFirstTime)
                stayTicks = MathHelper.getInt(player.getRNG(),
                        ForgeConfigHandler.teleporter.glitchDurationMin,
                        ForgeConfigHandler.teleporter.glitchDurationMax
                );
            stayTicks *= 20; // seconds to ticks

            persistedData.setLong(TeleportNBTKeys.RETURN_TIME, DimensionManager.getWorld(0).getTotalWorldTime() + stayTicks);

            toggleInvincibility(playerMP, true); // Eagle is playing dangerous games
        }
    }

    /** Called from player tick: teleports the player back to stored finalPos and removes immunity. */
    public static void returnToTeleporter(EntityPlayerMP player) {
        NBTTagCompound persistedData = TeleportService.getPlayerPersistedData(player);
        if (!persistedData.hasKey(TeleportNBTKeys.RETURN_TIME)) return;
        if (DimensionManager.getWorld(0).getTotalWorldTime() < persistedData.getLong(TeleportNBTKeys.RETURN_TIME)) return;

        toggleInvincibility(player, false);

        int originDim = persistedData.getInteger(TeleportNBTKeys.RETURN_DIM);
        if (!DimensionManager.isDimensionRegistered(originDim)) {
            EagleMixins.LOGGER.error("Glitch return failed: dimension {} not registered.", originDim);
            return;
        }

        double x = persistedData.getDouble(TeleportNBTKeys.RETURN_X);
        double y = persistedData.getDouble(TeleportNBTKeys.RETURN_Y);
        double z = persistedData.getDouble(TeleportNBTKeys.RETURN_Z);

        WorldServer target = DimensionManager.getWorld(originDim);
        if (target == null) return;

        if(player.dimension != target.provider.getDimension())
            player.getServer().getPlayerList().transferPlayerToDimension(player, originDim, new GlitchedPortalTeleporter(target));
        TeleportService.teleportWithSickness(player, x, y, z);

        persistedData.removeTag(TeleportNBTKeys.RETURN_TIME);
        persistedData.removeTag(TeleportNBTKeys.RETURN_DIM);
        persistedData.removeTag(TeleportNBTKeys.RETURN_X);
        persistedData.removeTag(TeleportNBTKeys.RETURN_Y);
        persistedData.removeTag(TeleportNBTKeys.RETURN_Z);
    }

    private static void toggleInvincibility(EntityPlayerMP player, boolean setToInvincible){
        if(!player.capabilities.isCreativeMode && !player.isSpectator()) {
            player.capabilities.disableDamage = setToInvincible;
            player.sendPlayerAbilities();
        }
    }

    public static BlockPos findSafeGlitchPosition(WorldServer world) {
        // Random position in a square ring between 5k and 15k from 0,0
        int x = (world.rand.nextBoolean() ? 1 : -1) * (5000 + world.rand.nextInt(10000));
        int z = (world.rand.nextBoolean() ? 1 : -1) * (5000 + world.rand.nextInt(10000));

        for (int y = 150; y > 10; y--) {
            BlockPos floorPos = new BlockPos(x, y, z);
            BlockPos feetPos = floorPos.up();
            BlockPos headPos = floorPos.up(2);

            if (world.getBlockState(floorPos).isOpaqueCube() &&
                    (world.isAirBlock(feetPos) || world.getBlockState(feetPos).getMaterial().isLiquid()) &&
                    (world.isAirBlock(headPos) || world.getBlockState(headPos).getMaterial().isLiquid())) {
                return feetPos;
            }
        }

        return world.getTopSolidOrLiquidBlock(new BlockPos(x, 255, z)); // Let them fall, they don't take dmg anyway
    }

    public static final class GlitchedPortalTeleporter extends Teleporter {
        public GlitchedPortalTeleporter(WorldServer worldIn) { super(worldIn); }

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
