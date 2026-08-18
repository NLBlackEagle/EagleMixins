package eaglemixins.teleport.entity;

import eaglemixins.EagleMixins;
import eaglemixins.teleport.TeleportData;
import eaglemixins.teleport.TeleportDataHandler;
import eaglemixins.teleport.TeleportNBTKeys;
import eaglemixins.teleport.TeleportService;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import svenhjol.charm.world.feature.EndPortalRunes;

public class EntitySpawnListener extends Entity {

    private int linkId = -1;
    private boolean isSender = false;
    private boolean wasActivated = false;

    public static void init() {
        EntityRegistry.registerModEntity(
                new ResourceLocation(EagleMixins.MODID, "spawn_listener"),
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
        this.linkId = c.getInteger(TeleportNBTKeys.ENTITY_LINKID);
        this.isSender = c.getBoolean(TeleportNBTKeys.ENTITY_ISSENDER);
    }
    @Override protected void writeEntityToNBT(NBTTagCompound c) {
        c.setInteger(TeleportNBTKeys.ENTITY_LINKID, this.linkId);
        c.setBoolean(TeleportNBTKeys.ENTITY_ISSENDER, this.isSender);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) return;

        if (!this.wasActivated) { // should only activate once anyway bc of setDead but let's be extra safe ig
            this.wasActivated = true;
            BlockPos center = this.getPosition().down();

            // Activate EndPortalRunes around the pad center
            if(Loader.isModLoaded("charm")) activateRunes(center);

            TeleportDataHandler dataHandler = TeleportDataHandler.get(world);
            TeleportData data = dataHandler.getTeleportData(linkId);

            if (isSender)
                data.setDiscoveredSenderPos(center);
            else { // receiver
                data.setDiscoveredReceiverPos(center);
            }
            dataHandler.markDirty(); // save new positions to .dat

            // A player might have generated this entity with the chunk when they were teleporting to the approx coord nearby.
            // Now teleport them to the exact location of the teleporter.
            TeleportService.enqueueToReTeleport((EntityPlayerMP) world.getClosestPlayer(this.posX, this.posY, this.posZ, -1, e -> e instanceof EntityPlayerMP));

            setDead();
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
