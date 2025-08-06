package eaglemixins.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraft.util.text.TextComponentTranslation;

public class FallDamageNegation {

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        EntityPlayer player = event.player;

        if (event.toDim == 3) {
            long currentTime = player.world.getTotalWorldTime();
            player.getEntityData().setLong("eaglemixins_lastDim3Teleport", currentTime);
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (event.getSource() != DamageSource.FALL) return;
        if (player.world.provider.getDimension() != 3) return;

        NBTTagCompound data = player.getEntityData();
        if (!data.hasKey("eaglemixins_lastDim3Teleport")) return;

        long lastTeleport = data.getLong("eaglemixins_lastDim3Teleport");
        long now = player.world.getTotalWorldTime();

        if (now - lastTeleport <= 60) {
            event.setAmount(event.getAmount() * 0.7f);

            // ✅ Remove the tag after applying the reduction
            data.removeTag("eaglemixins_lastDim3Teleport");
            player.sendMessage(new TextComponentTranslation("eaglemixins.message.dimensional.fall_damage"));
        }
    }
}
