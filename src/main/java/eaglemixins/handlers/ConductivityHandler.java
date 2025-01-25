package eaglemixins.handlers;

import eaglemixins.EagleMixins;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ConductivityHandler {
    private static final String savedTimeKey = "LightningLastSavedTime";
    private static final String hasWarnedKey = "LightningHasWarned";   //false = after strike, waiting for warning, true = after warning, waiting until strike
    private static PotionEffect lightning = null;
    public static PotionEffect getLightning() {
        if (lightning == null) {
            Potion potion = Potion.getPotionFromResourceLocation("potioncore:lightning");
            if (potion != null)
                lightning = new PotionEffect(potion, 1, 0);
        }
        return lightning;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
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
        //TODO: might need to subtract more than 1 from y position to actually get the block below the player
        boolean doShock = isThundering || player.getRidingEntity() != null || world.getBlockState(player.getPosition().add(0, -1, 0)).getBlock().equals(Blocks.AIR);
        if (!doShock) {
            resetLightningNBT(player, worldTime);
            return;
        }

        int conductivityOnEquipment = 0;
        for (ItemStack stack : player.getEquipmentAndArmor()) {
            if (stack.equals(ItemStack.EMPTY)) continue;
            Item item = stack.getItem();
            conductivityOnEquipment += ForgeConfigHandler.getItemConductivity(item);
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
            //this added time was 60 ticks before but the event only checking every 100 ticks made it effectively 100 anyway
        } else if (worldTime > lastSavedTime + 100) {
            if(getLightning() != null)
                player.addPotionEffect(new PotionEffect(getLightning()));
            player.getEntityData().setLong(savedTimeKey, worldTime);
            player.getEntityData().setBoolean(hasWarnedKey, false);
        }
    }

    private static void resetLightningNBT(EntityPlayer player, long worldTime) {
        //Setting to 0 would mean players lose their old cooldown if they get to safety, making next warning happen earlier when they get back into the rain right away
        //By setting to worldTime instead, players will always have at least 400/1200 ticks of safety in the rain. only after that the warning checks will happen
        player.getEntityData().setLong(savedTimeKey, worldTime);
        //next thing to happen is warning, only after that comes next strike
        player.getEntityData().setBoolean(hasWarnedKey, false);
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        int conductivity = ForgeConfigHandler.getItemConductivity(event.getItemStack().getItem());
        if (conductivity > 0)
            event.getToolTip().add("" + TextFormatting.YELLOW + TextFormatting.ITALIC + "Conductivity +" + conductivity);
    }
}