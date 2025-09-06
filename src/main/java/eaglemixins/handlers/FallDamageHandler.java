package eaglemixins.handlers;

import eaglemixins.EagleMixins;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Mod.EventBusSubscriber(modid = EagleMixins.MODID)
public final class FallDamageHandler {

    private static final long GRACE_TICKS = 200L;      // 10s @ 20 TPS
    private static final float MIN_HEALTH_AFTER = 1.0F; // 0.5 hearts

    private static volatile long SERVER_TICK = 0L;
    private static final ConcurrentMap<UUID, Long> GRACE_UNTIL_TICK = new ConcurrentHashMap<>();

    private FallDamageHandler() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.END) SERVER_TICK++;
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (!(e.player instanceof EntityPlayerMP)) return;
        if (e.player.world.isRemote) return;
        GRACE_UNTIL_TICK.put(e.player.getUniqueID(), SERVER_TICK + GRACE_TICKS);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        GRACE_UNTIL_TICK.remove(e.player.getUniqueID());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        GRACE_UNTIL_TICK.remove(e.player.getUniqueID());
    }

    // Clamp at LivingHurt (some stacks use this path)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        clampNonLethal(event.getEntityLiving() instanceof EntityPlayer ? (EntityPlayer) event.getEntityLiving() : null,
                event.getSource(), event::getAmount, event::setAmount);
    }

    // Clamp at LivingDamage (final line of defense)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        clampNonLethal(event.getEntityLiving() instanceof EntityPlayer ? (EntityPlayer) event.getEntityLiving() : null,
                event.getSource(), event::getAmount, event::setAmount);
    }

    private static void clampNonLethal(EntityPlayer player, DamageSource src,
                                       java.util.function.Supplier<Float> getter,
                                       java.util.function.Consumer<Float> setter) {
        if (player == null || player.world.isRemote) return;

        Long until = GRACE_UNTIL_TICK.get(player.getUniqueID());
        if (until == null || SERVER_TICK > until) return;

        // Only constrain impact-like landings; include elytra collisions.
        if (!(src == DamageSource.FALL || src == DamageSource.FLY_INTO_WALL)) return;

        float health = player.getHealth();
        float amount = getter.get();
        float maxAllowed = Math.max(0F, health - MIN_HEALTH_AFTER);

        if (amount > maxAllowed) setter.accept(maxAllowed); // ensures survival, still takes damage
    }
}
