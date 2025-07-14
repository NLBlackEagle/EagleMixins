package eaglemixins.util;

import net.minecraft.entity.Entity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class Ref {
    public static final String SRPMODID = "srparasites";

    public static final ResourceLocation dragonBossReg = new ResourceLocation("srparasites:sim_dragone");

    public static final ResourceLocation deadBloodReg = new ResourceLocation(SRPMODID+":deadblood");

    public static final ResourceLocation abyssalRiftReg = new ResourceLocation("openterraingenerator:overworld_abyssal_rift");

    public static final ResourceLocation abyssalGateReg = new ResourceLocation("openterraingenerator:overworld_abyssal_gate");

    public static final ResourceLocation parasiteBiomeReg = new ResourceLocation(SRPMODID+":biome_parasite");

    public static boolean entityIsInAbyssalRift(Entity entity){
        ResourceLocation biomeReg = entity.world.getBiome(entity.getPosition()).getRegistryName();
        return biomeReg != null && biomeReg.equals(abyssalRiftReg);
    }

    public static boolean entityIsInAbyssalGate(Entity entity){
        ResourceLocation biomeReg = entity.world.getBiome(entity.getPosition()).getRegistryName();
        return biomeReg != null && biomeReg.equals(abyssalGateReg);
    }

    private static PotionEffect lightning = null;
    public static PotionEffect getLightning() {
        if (lightning == null) {
            Potion potion = Potion.getPotionFromResourceLocation("potioncore:lightning");
            if (potion != null)
                lightning = new PotionEffect(potion, 1, 0);
        }
        return lightning;
    }
}
