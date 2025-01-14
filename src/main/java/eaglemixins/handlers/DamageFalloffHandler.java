package eaglemixins.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DamageFalloffHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event){
        if(!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        float amount = event.getAmount();

        //Breakpoints: divide by 1 for dmg<=20, divide by 3 for dmg>=300, in between divisor increases linearly
        float divisor = MathHelper.clamp(6.F/7.F+amount/140.F,1F,3F);
        event.setAmount(amount/divisor);
    }
}
