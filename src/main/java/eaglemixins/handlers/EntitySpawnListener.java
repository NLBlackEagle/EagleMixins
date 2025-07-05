package eaglemixins.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import eaglemixins.EagleMixins;
import eaglemixins.network.PacketStartTeleportOverlay;
import eaglemixins.network.PacketStopTeleportOverlay;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import svenhjol.charm.world.feature.EndPortalRunes;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class EntitySpawnListener extends Entity {

    private static final Logger LOGGER = LogManager.getLogger("EagleMixins");

    private static final Map<Integer, TeleportData> LINK_ID_DESTINATIONS = new HashMap<>();
    private static File configFile;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static boolean configInitialized = false;

    private int linkId = -1;
    private boolean isSender = false;
    private boolean isReceiver = false;

    private static boolean randomChance(World world) {
        return world.rand.nextInt(100) < 1;
    }

    public static void init() {
        int entityID = 128;
        EntityRegistry.registerModEntity(
                new ResourceLocation(EagleMixins.MODID, "spawn_listener"),
                EntitySpawnListener.class,
                "SpawnListener",
                entityID,
                EagleMixins.INSTANCE,
                64,
                1,
                false
        );
    }

    public EntitySpawnListener(World worldIn) {
        super(worldIn);
        this.setSize(0.1F, 0.1F);
        this.noClip = true;
        this.setInvisible(true);
    }

    @Override
    protected void entityInit() {}

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.linkId = compound.getInteger("linkId");
        this.isSender = compound.getBoolean("isSender");
        this.isReceiver = compound.getBoolean("isReceiver");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("linkId", this.linkId);
        compound.setBoolean("isSender", this.isSender);
        compound.setBoolean("isReceiver", this.isReceiver);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (world.isRemote) return;

        ensureConfigInitialized();

        if (!this.getEntityData().getBoolean("activated")) {
            this.getEntityData().setBoolean("activated", true);

            BlockPos center = this.getPosition().down();
            int radius = 2;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) continue;
                    if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;

                    BlockPos targetPos = center.add(dx, 0, dz);
                    EndPortalRunes.activate(world, targetPos);
                }
            }

            BlockPos pos = this.getPosition().down();
            TeleportData data = LINK_ID_DESTINATIONS.getOrDefault(linkId, new TeleportData());

            //LOGGER.info("[EagleMixins] EntitySpawnListener activated at {} with isSender={}, linkId={}", pos, isSender, linkId);

            if (isSender) {
                data.sender = pos;
                if (data.tempReceiver == null) {
                    data.tempReceiver = getTempReceiverCoordsFor(linkId);
                }

            } else if (isReceiver && data.receiver == null) {
                data.receiver = pos;


                for (EntityPlayer nearbyPlayer : world.playerEntities) {
                    NBTTagCompound persistTag = nearbyPlayer.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
                    if (persistTag.getBoolean("justTeleported") && persistTag.getInteger("linkId") == linkId) {
                        BlockPos finalPos = pos.add(0, 3, 3);
                        nearbyPlayer.setPositionAndUpdate(finalPos.getX() + 0.5, finalPos.getY(), finalPos.getZ() + 0.5);

                        if (nearbyPlayer instanceof EntityPlayerMP) {
                            EagleMixins.NETWORK.sendTo(new PacketStopTeleportOverlay(), (EntityPlayerMP) nearbyPlayer);
                        }

                        //LOGGER.info("[EagleMixins] Final teleport complete for player {} to {}", nearbyPlayer.getName(), finalPos);

                        persistTag.removeTag("justTeleported");
                        persistTag.removeTag("linkId");
                        nearbyPlayer.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistTag);
                        break;
                    }
                }
            }

            LINK_ID_DESTINATIONS.put(linkId, data);
            saveDestinationsToDisk();
            this.setDead();
        }
    }

    private static BlockPos getTempReceiverCoordsFor(int linkId) {
        switch (linkId) {
            case 0: return new BlockPos(-8247, 80, -12929); // Frozen Greens
            case 1: return new BlockPos(-14345, 80, -6923); // The Highlands
            case 2: return new BlockPos(-14394, 80, 7793);  // Valley of Sulfur
            case 3: return new BlockPos(-2363, 80, 15883);  // Southern Green
            case 4: return new BlockPos(13505, 80, -8554);  // Sea of Decay
            case 5: return new BlockPos(3303, 80, -13134);  // Green Desert
            case 6: return new BlockPos(7988, 80, 13359);   // Permafrost
            default: return new BlockPos(0, 80, 0);
        }
    }

    private static BlockPos getTempSenderCoordsFor(int linkId) {
        switch (linkId) {
            case 0: return new BlockPos(-950, 80, -2228);   // Frozen Greens
            case 1: return new BlockPos(-2355, 80, -642);   // The Highlands
            case 2: return new BlockPos(-1987, 80, 1453);   // Valley of Sulfur
            case 3: return new BlockPos(-116, 80, 2455);    // Southern Green
            case 4: return new BlockPos(2407, 80, -401);    // Sea of Decay
            case 5: return new BlockPos(1171, 80, -2126);   // Green Desert
            case 6: return new BlockPos(1831, 80, 1635);    // Permafrost
            default: return new BlockPos(0, 80, 0);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (player.world.isRemote || !(player instanceof EntityPlayerMP)) return;

        NBTTagCompound tag = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);

        if (player.dimension == 3 && tag.hasKey("glitchEndTime")) {
            long endTime = tag.getLong("glitchEndTime");
            if (player.world.getTotalWorldTime() >= endTime) {
                int originDim = tag.getInteger("glitchOriginDim");

                if (!DimensionManager.isDimensionRegistered(originDim)) {
                    EagleMixins.LOGGER.error("[EagleMixins] Glitch return failed: dimension {} not registered.", originDim);
                    return;
                }

                double x = tag.getDouble("glitchReturnX");
                double y = tag.getDouble("glitchReturnY");
                double z = tag.getDouble("glitchReturnZ");


                WorldServer targetWorld = DimensionManager.getWorld(originDim);
                if (targetWorld != null) {
                    EntityPlayerMP mp = (EntityPlayerMP) player;

                    // Clear invulnerability
                    mp.capabilities.disableDamage = false;
                    mp.sendPlayerAbilities();

                    // Add Overlay for teleport
                    if (player instanceof EntityPlayerMP) {
                        EagleMixins.NETWORK.sendTo(new PacketStartTeleportOverlay(true), (EntityPlayerMP) player);
                    }

                    // Teleport
                    Objects.requireNonNull(mp.getServer()).getPlayerList().transferPlayerToDimension(mp, originDim, new DummyTeleporter(targetWorld));
                    mp.setPositionAndUpdate(x, y, z);

                    // Stop the teleport overlay
                    EagleMixins.NETWORK.sendTo(new PacketStopTeleportOverlay(), mp);

                    // Clean up
                    tag.removeTag("glitchEndTime");
                    tag.removeTag("glitchOriginDim");
                    tag.removeTag("glitchReturnX");
                    tag.removeTag("glitchReturnY");
                    tag.removeTag("glitchReturnZ");
                    player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);

                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTeleport(PlayerEvent event) {
        if (!(event.getEntityPlayer() instanceof EntityPlayer)) return;

        EntityPlayer player = event.getEntityPlayer();
        World world = player.world;

        if (world.isRemote) return;

        ensureConfigInitialized();

        boolean foundPortal = false;
        BlockPos basePos = player.getPosition().down();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos checkPos = basePos.add(dx, 0, dz);
                Block block = world.getBlockState(checkPos).getBlock();
                if (block instanceof BlockEndPortal) {
                    foundPortal = true;
                    //LOGGER.info("[EagleMixins] Found end portal block at {}", checkPos);
                    break;
                }
            }
            if (foundPortal) break;
        }

        if (!foundPortal) {
            BlockPos below = basePos.down();
            Block block = world.getBlockState(below).getBlock();
            if (block instanceof BlockEndPortal) {
                foundPortal = true;
                //LOGGER.info("[EagleMixins] Found end portal block 1 block below at {}", below);
            }
        }

        if (!foundPortal) return;

        NBTTagCompound persistTag = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);

        for (Map.Entry<Integer, TeleportData> entry : LINK_ID_DESTINATIONS.entrySet()) {
            TeleportData data = entry.getValue();
            int linkId = entry.getKey();

            if (data.sender != null && player.getDistanceSq(data.sender) <= 36) {

                BlockPos returnPos = data.receiver != null ? data.receiver : getTempSenderCoordsFor(linkId);

                //LOGGER.info("[EagleMixins] Underneath Teleport Pass Chance: {}, Receiver is known: {}", randomChance(world), data.receiver != null);

                if (randomChance(world) && data.receiver != null) {


                    NBTTagCompound tag = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
                    tag.setDouble("glitchReturnX", returnPos.getX() + 0.5);
                    tag.setDouble("glitchReturnY", returnPos.getY() + 2);
                    tag.setDouble("glitchReturnZ", returnPos.getZ() + 3.5);
                    tag.setInteger("glitchOriginDim", player.dimension);
                    player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);

                    if (player instanceof EntityPlayerMP) {
                        EagleMixins.NETWORK.sendTo(new PacketStartTeleportOverlay(true), (EntityPlayerMP) player);
                        player.addStat(ModStats.GLITCH_COUNT);
                    }

                    triggerGlitchTeleport(player);
                    return;
                }

                if (data.receiver != null) {
                    // Exact receiver coords
                    player.setPositionAndUpdate(
                            data.receiver.getX() + 0.5,
                            data.receiver.getY() + 2,
                            data.receiver.getZ() + 3.5);
                } else if (data.tempReceiver != null) {

                    if (player instanceof EntityPlayerMP) {
                        EagleMixins.NETWORK.sendTo(new PacketStartTeleportOverlay(false), (EntityPlayerMP) player);
                    }

                    // Fallback: safe surface block
                    BlockPos safePos = world.getTopSolidOrLiquidBlock(new BlockPos(data.tempReceiver.getX(), 0, data.tempReceiver.getZ()));
                    player.setPositionAndUpdate(
                            safePos.getX() + 0.5,
                            safePos.getY(),
                            safePos.getZ() + 0.5);
                } else {
                    continue; // No destination
                }

                persistTag.setBoolean("justTeleported", true);
                persistTag.setInteger("linkId", linkId);
                player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistTag);
                return;
            } else if (data.receiver != null && player.getDistanceSq(data.receiver) <= 36) {

                BlockPos returnPos = data.sender != null ? data.sender : getTempSenderCoordsFor(linkId);

                if (randomChance(world) && data.sender != null) {

                    NBTTagCompound tag = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
                    tag.setDouble("glitchReturnX", returnPos.getX() + 0.5);
                    tag.setDouble("glitchReturnY", returnPos.getY() + 2);
                    tag.setDouble("glitchReturnZ", returnPos.getZ() + 3.5);
                    tag.setInteger("glitchOriginDim", player.dimension);
                    player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);


                    if (player instanceof EntityPlayerMP) {
                        EagleMixins.NETWORK.sendTo(new PacketStartTeleportOverlay(true), (EntityPlayerMP) player);
                        player.addStat(ModStats.GLITCH_COUNT);
                    }

                    triggerGlitchTeleport(player);
                    return;
                }

                if (data.sender != null) {
                    // Use exact sender coords with +1 Y
                    player.setPositionAndUpdate(
                            data.sender.getX() + 0.5,
                            data.sender.getY() + 2,
                            data.sender.getZ() + 3.5
                    );
                } else {

                    if (player instanceof EntityPlayerMP) {
                        EagleMixins.NETWORK.sendTo(new PacketStartTeleportOverlay(false), (EntityPlayerMP) player);
                    }

                    // Fallback: use temp sender with safe Y
                    BlockPos fallback = getTempSenderCoordsFor(linkId);
                    BlockPos safePos = world.getTopSolidOrLiquidBlock(
                            new BlockPos(fallback.getX(), 0, fallback.getZ())
                    );
                    player.setPositionAndUpdate(
                            safePos.getX() + 0.5,
                            safePos.getY() + 0.5,
                            safePos.getZ() + 0.5
                    );
                }

                persistTag.setBoolean("justTeleported", true);
                persistTag.setInteger("linkId", linkId);
                player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistTag);
                return;
            }
        }

        LOGGER.warn("[EagleMixins] No matching portal position found for player {}", player.getName());
    }

    private static void saveDestinationsToDisk() {
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(LINK_ID_DESTINATIONS, writer);
        } catch (IOException e) {
            LOGGER.error("[EagleMixins] Failed to save teleport destinations: {}", e.getMessage());
        }
    }

    private static void loadDestinationsFromDisk() {
        if (configFile == null || !configFile.exists()) return;
        try (FileReader reader = new FileReader(configFile)) {
            Type type = new TypeToken<Map<Integer, TeleportData>>() {}.getType();
            Map<Integer, TeleportData> loaded = GSON.fromJson(reader, type);
            if (loaded != null) LINK_ID_DESTINATIONS.putAll(loaded);
        } catch (IOException e) {
            LOGGER.error("[EagleMixins] Failed to load teleport destinations: {}", e.getMessage());
        }

        // Register fallback linkIds 0–6 if missing
        registerDefaultLinkIfMissing(0, new BlockPos(-950, 80, -2228), new BlockPos(-8247, 80, -12929)); // Frozen Greens
        registerDefaultLinkIfMissing(1, new BlockPos(-2355, 80, -642), new BlockPos(-14345, 80, -6923)); // The Highlands
        registerDefaultLinkIfMissing(2, new BlockPos(-1987, 80, 1453), new BlockPos(-14394, 80, 7793)); // Valley of Sulfur
        registerDefaultLinkIfMissing(3, new BlockPos(-116, 80, 2455), new BlockPos(-2363, 80, 15883)); // Southern Green
        registerDefaultLinkIfMissing(4, new BlockPos(2407, 80, -401), new BlockPos(13505, 80, -8554)); // Sea of Decay
        registerDefaultLinkIfMissing(5, new BlockPos(1171, 80, -2126), new BlockPos(3303, 80, -13134)); // Green Desert
        registerDefaultLinkIfMissing(6, new BlockPos(1831, 80, 1635), new BlockPos(7988, 80, 13359)); // Permafrost

        saveDestinationsToDisk(); // Persist changes if any were added


    }

    public static void reloadConfigFile() {
        World overworld = DimensionManager.getWorld(0); // get DIM0 (main world)
        if (overworld != null) {
            File saveFolder = overworld.getSaveHandler().getWorldDirectory();
            configFile = new File(saveFolder, "eaglemixins_teleports.json");
            //LOGGER.info("[EagleMixins] Using world-specific teleport config path: {}", configFile.getAbsolutePath());
        } else {
            LOGGER.warn("[EagleMixins] World not loaded yet — defaulting to root.");
            configFile = new File("eaglemixins_teleports.json");
        }
    }

    private static void ensureConfigInitialized() {
        if (!configInitialized) {
            reloadConfigFile();
            loadDestinationsFromDisk();
            configInitialized = true;
        }
    }

    private static void registerDefaultLinkIfMissing(int linkId, BlockPos sender, BlockPos receiver) {
        if (!LINK_ID_DESTINATIONS.containsKey(linkId)) {
            TeleportData data = new TeleportData();
            data.sender = sender;
            data.tempReceiver = receiver;
            LINK_ID_DESTINATIONS.put(linkId, data);
            //LOGGER.info("[EagleMixins] Inserted fallback teleport data for linkId {} -> sender: {}, receiver: {}", linkId, sender, receiver);
        }
    }

    private static void triggerGlitchTeleport(EntityPlayer player) {

        int glitchDim = 3;
        int overworldDim = 0;

        if (!DimensionManager.isDimensionRegistered(glitchDim)) {
            LOGGER.error("[EagleMixins] Glitch dimension {} is not registered!", glitchDim);
            return;
        }
        if (!DimensionManager.isDimensionRegistered(overworldDim)) {
            LOGGER.error("[EagleMixins] Overworld dimension {} is not registered!", overworldDim);
            return;
        }

        WorldServer glitchWorld = DimensionManager.getWorld(glitchDim);
        if (glitchWorld == null) {
            DimensionManager.initDimension(glitchDim);
            glitchWorld = DimensionManager.getWorld(glitchDim);
        }

        WorldServer overWorld = DimensionManager.getWorld(overworldDim);
        if (overWorld == null) {
            DimensionManager.initDimension(overworldDim);
            overWorld = DimensionManager.getWorld(overworldDim);
        }

        if (glitchWorld == null || overWorld == null) {
            LOGGER.error("[EagleMixins] Could not access glitch or overworld dimension after init.");
            return;
        }

        NBTTagCompound tag = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        tag.setBoolean("glitchTeleported", true);
        tag.setLong("glitchStartTime", System.currentTimeMillis());
        player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);

        // Example glitch coordinates (near spawn or static)
        BlockPos glitchSpawn = new BlockPos(0, 100, 0);
        player.setPositionAndUpdate(glitchSpawn.getX() + 0.5, glitchSpawn.getY(), glitchSpawn.getZ() + 0.5);

        // Transfer player to dimension 3
        if (!player.world.isRemote && player instanceof EntityPlayerMP) {
            EntityPlayerMP mp = (EntityPlayerMP) player;
            Objects.requireNonNull(mp.getServer()).getPlayerList().transferPlayerToDimension(mp, 3, new DummyTeleporter(glitchWorld));

            // Check if this is the first glitch
            boolean firstTime = !tag.getBoolean("glitchDone");
            tag.setBoolean("glitchDone", true); // Mark it done

            // Set duration in ticks: 5s if first time, 7–20s otherwise
            long stayTimeTicks = firstTime ? 140 : 140 + player.world.rand.nextInt(260);
            tag.setLong("glitchEndTime", player.world.getTotalWorldTime() + stayTimeTicks);

            // Apply changes
            mp.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);

            // Grant invulnerability while in glitch world
            mp.capabilities.disableDamage = true;
            mp.sendPlayerAbilities();
        }

    }

    public static class DummyTeleporter extends net.minecraft.world.Teleporter {

        public DummyTeleporter(WorldServer worldIn) {
            super(worldIn);
        }

        @Override
        public void placeInPortal(Entity entity, float rotationYaw) {
            BlockPos pos = findSafeGlitchPosition(this.world);
            entity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            entity.motionX = 0;
            entity.motionY = 0;
            entity.motionZ = 0;
        }

        @Override
        public boolean placeInExistingPortal(Entity entity, float rotationYaw) {
            return true; // We pretend the portal "exists"
        }

        @Override
        public boolean makePortal(Entity entity) {
            return true; // Skip portal creation logic
        }

        @Override
        public void removeStalePortalLocations(long worldTime) {
            // No-op
        }
    }

    private static BlockPos findSafeGlitchPosition(WorldServer world) {
        Random rand = new Random();

        for (int attempt = 0; attempt < 100; attempt++) {
            int x = (rand.nextBoolean() ? 1 : -1) * (5000 + rand.nextInt(10000));
            int z = (rand.nextBoolean() ? 1 : -1) * (5000 + rand.nextInt(10000));

            for (int y = 150; y > 10; y--) {
                BlockPos floorPos = new BlockPos(x, y, z);
                BlockPos above1 = floorPos.up();
                BlockPos above2 = floorPos.up(2);

                if (world.getBlockState(floorPos).isOpaqueCube() &&
                        (world.isAirBlock(above1) || world.getBlockState(above1).getMaterial().isLiquid()) &&
                        (world.isAirBlock(above2) || world.getBlockState(above2).getMaterial().isLiquid())) {

                    return above1;
                }
            }

            // fallback to top solid block
            BlockPos top = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
            if (top != null) return top.up();
        }

        return new BlockPos(0, 250, 0); // absolute fallback
    }

    public static class TeleportData {
        public BlockPos sender;
        public BlockPos tempReceiver;
        public BlockPos receiver;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }
}
