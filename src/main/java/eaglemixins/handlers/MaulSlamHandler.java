package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.SRParasiteConfig;
import eaglemixins.network.PacketHandler;
import eaglemixins.network.PacketMaulSlam;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The Sentient Maul's ability triggers when its hit kills the target (SRP's own AoE, or RLMixins' sweep rehandle).
 * That kill slams the ground: sounds here, the earthquake wave and dust on the clients (ClientEffects).
 * Kills made by the ability itself come right after, the cooldown keeps those from slamming again.
 */
public class MaulSlamHandler {

    private static final Map<UUID, Long> lastSlam = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        SRParasiteConfig.MaulSlamConfig config = ForgeConfigHandler.srparasites.maulSlam;
        if (!config.enabled) return;
        EntityLivingBase victim = event.getEntityLiving();
        World world = victim.world;
        if (world.isRemote) return;

        Entity source = event.getSource().getTrueSource();
        if (!(source instanceof EntityPlayer) || event.getSource().getImmediateSource() != source) return;
        EntityPlayer player = (EntityPlayer) source;
        ItemStack held = player.getHeldItemMainhand();
        if (held.isEmpty() || !config.isSlamWeapon(held.getItem().getRegistryName())) return;

        long now = world.getTotalWorldTime();
        Long last = lastSlam.get(player.getUniqueID());
        if (last != null && now - last >= 0 && now - last < config.cooldown) return;
        lastSlam.put(player.getUniqueID(), now);

        double x = victim.posX, y = victim.posY, z = victim.posZ;
        world.playSound(null, x, y, z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.9F, 0.55F);
        world.playSound(null, x, y, z, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.45F, 0.5F);
        world.playSound(null, x, y, z, SoundEvents.ENTITY_IRONGOLEM_ATTACK, SoundCategory.PLAYERS, 1.0F, 0.6F);
        PacketHandler.sendToAllAround(new PacketMaulSlam(x, y, z, config.radius), world.provider.getDimension(), x, y, z, 96);
    }
}
