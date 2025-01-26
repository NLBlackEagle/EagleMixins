package eaglemixins.handlers;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DispelEntityHandler {
    private static final List<String> effectStrings = Arrays.asList(
            "potioncore:dispel",
            "biomesoplenty:curse"
    );
    private static List<PotionEffect> effects = null;
    private static PotionEffect getEffect(int id) {
        if (effects == null) {
            effects = new ArrayList<>();
            for (String effectString : effectStrings) {
                Potion potion = Potion.getPotionFromResourceLocation(effectString);
                if (potion != null)
                    effects.add(new PotionEffect(potion, 100, 0));
            }
        }
        if (id > 0 && id < effects.size())
            return new PotionEffect(effects.get(id));
        else
            return new PotionEffect(effects.get(0));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntityLiving().world.isRemote) return;
        if (event.getSource() == null) return;
        if (event.getSource().getTrueSource() == null) return;
        if (!(event.getSource().getTrueSource() instanceof EntityLivingBase)) return;
        EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();
        if (!attacker.hasCustomName()) return;
        EntityLivingBase victim = event.getEntityLiving();

        if (attacker.getName().contains("Dispel") || attacker.getName().contains("Sarevok"))
            victim.addPotionEffect(getEffect(0));   //Dispel
        if (attacker.getName().contains("Sarevok"))
            victim.addPotionEffect(getEffect(1));   //Curse
    }
}
