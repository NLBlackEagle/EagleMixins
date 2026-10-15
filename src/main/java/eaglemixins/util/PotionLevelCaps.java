package eaglemixins.util;

import eaglemixins.EagleMixins;
import eaglemixins.config.ForgeConfigHandler;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/** Clamps potion effect amplifiers to the levels configured in "Potion Level Caps". */
public class PotionLevelCaps {

    private static Map<Potion, Integer> maxAmplifiers = null;

    private static Map<Potion, Integer> getMaxAmplifiers() {
        if (maxAmplifiers == null) {
            maxAmplifiers = new HashMap<>();
            for (Map.Entry<ResourceLocation, Integer> entry : ForgeConfigHandler.server.potionLevelCaps.entrySet()) {
                Potion potion = ForgeRegistries.POTIONS.getValue(entry.getKey());
                if (potion == null) {
                    EagleMixins.LOGGER.warn("[Potion Level Caps] Unknown potion {}", entry.getKey());
                    continue;
                }
                //Config uses displayed levels (1 = level I), amplifier is 0-based
                maxAmplifiers.put(potion, Math.max(0, entry.getValue() - 1));
            }
        }
        return maxAmplifiers;
    }

    /** Returns the given effect, or a copy with its amplifier lowered to the configured cap. */
    public static PotionEffect cap(PotionEffect effect) {
        if (effect == null) return null;
        Integer maxAmplifier = getMaxAmplifiers().get(effect.getPotion());
        if (maxAmplifier == null || effect.getAmplifier() <= maxAmplifier) return effect;

        PotionEffect capped = new PotionEffect(effect.getPotion(), effect.getDuration(), maxAmplifier, effect.getIsAmbient(), effect.doesShowParticles());
        capped.setCurativeItems(effect.getCurativeItems());
        capped.setPotionDurationMax(effect.getIsPotionDurationMax());
        return capped;
    }

    public static void refreshConfig() {
        maxAmplifiers = null;
    }
}
