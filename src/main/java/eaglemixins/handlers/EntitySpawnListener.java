package eaglemixins.handlers;

import eaglemixins.EagleMixins;
import eaglemixins.teleport.TeleportData;
import eaglemixins.teleport.TeleportRegistry;
import eaglemixins.teleport.TeleportService;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import svenhjol.charm.world.feature.EndPortalRunes;

public class EntitySpawnListener extends Entity {

    private int linkId = -1;
    private boolean isSender = false;
    private boolean isReceiver = false;

    public static void init() {
        EntityRegistry.registerModEntity(
                new net.minecraft.util.ResourceLocation(EagleMixins.MODID, "spawn_listener"),
                EntitySpawnListener.class,
                "SpawnListener",
                128,
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

    @Override protected void entityInit() {}
    @Override protected void readEntityFromNBT(NBTTagCompound c) {
        this.linkId = c.getInteger("linkId");
        this.isSender = c.getBoolean("isSender");
        this.isReceiver = c.getBoolean("isReceiver");
    }
    @Override protected void writeEntityToNBT(NBTTagCompound c) {
        c.setInteger("linkId", this.linkId);
        c.setBoolean("isSender", this.isSender);
        c.setBoolean("isReceiver", this.isReceiver);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) return;

        TeleportRegistry.ensureInit();

        if (!this.getEntityData().getBoolean("activated")) {
            this.getEntityData().setBoolean("activated", true);

            // Activate EndPortalRunes around the pad center
            BlockPos center = this.getPosition().down();
            activateRunes(center);

            TeleportData data = TeleportRegistry.getOrCreate(linkId);

            if (isSender) {
                // was: data.sender = center; TeleportRegistry.markTempReceiverIfEmpty(linkId); TeleportRegistry.put(linkId, data);
                TeleportRegistry.updateSender(linkId, center);
                TeleportRegistry.markTempReceiverIfEmpty(linkId);

            } else if (isReceiver && data.receiver == null) {
                // was: data.receiver = center; TeleportRegistry.put(linkId, data);
                TeleportRegistry.updateReceiver(linkId, center);

                // unchanged: enqueue nearby player that justTeleported for this linkId
                for (EntityPlayer nearbyPlayer : world.playerEntities) {
                    NBTTagCompound persistTag = nearbyPlayer.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
                    if (persistTag.getBoolean("justTeleported") && persistTag.getInteger("linkId") == linkId) {
                        TeleportService.enqueue(nearbyPlayer);
                        break;
                    }
                }
            }

            TeleportRegistry.put(linkId, data);
            net.minecraft.server.MinecraftServer server = world.getMinecraftServer();
            if (server != null) server.addScheduledTask(this::setDead);
        }
    }

    private void activateRunes(BlockPos center) {
        int radius = 2;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) continue;
                if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                EndPortalRunes.activate(world, center.add(dx, 0, dz));
            }
        }
    }

    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean canBePushed() { return false; }
}
