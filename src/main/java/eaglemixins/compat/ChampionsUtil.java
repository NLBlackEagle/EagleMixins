package eaglemixins.compat;

import c4.champions.common.capability.CapabilityChampionship;
import c4.champions.common.capability.IChampionship;
import c4.champions.common.config.ConfigHandler;
import c4.champions.common.util.ChampionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

/** Only call when Champions is loaded (ModLoadedUtil.champions) */
public final class ChampionsUtil {

    /** Same checks Champions does before drawing its health HUD for the mob under the crosshair */
    public static boolean showsHealthHud(Entity entity) {
        if (ConfigHandler.hideEffects || !ChampionHelper.isValidChampion(entity)) return false;
        IChampionship championship = CapabilityChampionship.getChampionship((EntityLiving) entity);
        return championship != null && ChampionHelper.isElite(championship.getRank());
    }

    /** Champions' configured HUD y offset */
    public static int hudYOffset() {
        return ConfigHandler.client.yOffset;
    }

    private ChampionsUtil() {}
}
