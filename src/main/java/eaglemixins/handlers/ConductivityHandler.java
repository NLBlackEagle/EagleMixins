package eaglemixins.handlers;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.util.Ref;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConductivityHandler {
    private static final String savedTimeKey = "LightningLastSavedTime";
    private static final String hasWarnedKey = "LightningHasWarned";   //false = after strike, waiting for warning, true = after warning, waiting until strike

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(event.phase != TickEvent.Phase.START) return;
        EntityPlayer player = event.player;
        World world = player.world;
        long worldTime = world.getTotalWorldTime();
        if (worldTime % 100 != 0) return;
        if (world.isRemote) return;

        boolean isThundering = world.isThundering();
        if (!world.isRaining() || world.getLight(player.getPosition()) != 15) {
            resetLightningNBT(player, worldTime);
            return;
        }

        boolean doShock = isThundering || player.getRidingEntity() != null || world.isAirBlock(player.getPosition().add(0, -1, 0));
        if (!doShock) {
            resetLightningNBT(player, worldTime);
            return;
        }

        int conductivityOnEquipment = 0;
        for (ItemStack stack : player.getEquipmentAndArmor()) {
            if (stack.equals(ItemStack.EMPTY)) continue;
            conductivityOnEquipment += ForgeConfigHandler.getItemConductivity(stack);
        }

        if (!isThundering)
            conductivityOnEquipment /= 2;

        if (conductivityOnEquipment == 0) {
            resetLightningNBT(player, worldTime);
            return;
        }

        if (!player.getEntityData().hasKey(savedTimeKey)) player.getEntityData().setLong(savedTimeKey, worldTime);
        long lastSavedTime = player.getEntityData().getLong(savedTimeKey);
        if (!player.getEntityData().hasKey(hasWarnedKey)) player.getEntityData().setBoolean(hasWarnedKey, false);
        boolean hasWarned = player.getEntityData().getBoolean(hasWarnedKey);

        if (!hasWarned) {
            //each conductivity point is 0.5% or 1% per 100 ticks (5 seconds) to send a warning msg and strike after 100 ticks
            if (worldTime > lastSavedTime + (isThundering ? 400 : 1200) && player.getRNG().nextInt(100) < conductivityOnEquipment) {
                player.sendStatusMessage(new TextComponentTranslation("eaglemixins.conductivity."+player.getRNG().nextInt(8)), true);
                player.getEntityData().setLong(savedTimeKey, worldTime);
                player.getEntityData().setBoolean(hasWarnedKey, true);
            }
            //the event only checking every 100 ticks makes this effectively 100 ticks
        } else if (worldTime > lastSavedTime + 60) {
            if(Ref.getLightning() != null)
                player.addPotionEffect(new PotionEffect(Ref.getLightning()));
            player.getEntityData().setLong(savedTimeKey, worldTime);
            player.getEntityData().setBoolean(hasWarnedKey, false);
        }
    }

    private static void resetLightningNBT(EntityPlayer player, long worldTime) {
        //Setting to 0 would mean players lose their old cooldown (from last strike) if they get to safety, making next warning happen earlier when they get back into the rain right away
        //By setting to worldTime instead, players will always have at least 400/1200 ticks of safety in the rain after one strike. only after that the warning checks will happen
        player.getEntityData().setLong(savedTimeKey, worldTime);
        //first thing to happen is warning, only after that comes next strike
        player.getEntityData().setBoolean(hasWarnedKey, false);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onTooltip(ItemTooltipEvent event) {
        int conductivity = ForgeConfigHandler.getItemConductivity(event.getItemStack());
        if (conductivity > 0)
            event.getToolTip().add("" + TextFormatting.YELLOW + TextFormatting.ITALIC + I18n.format("eaglemixins.conductivity.tooltip",conductivity));
    }
}