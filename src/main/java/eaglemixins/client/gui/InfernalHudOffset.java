package eaglemixins.client.gui;

import eaglemixins.compat.ChampionsUtil;
import eaglemixins.compat.ModLoadedUtil;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * Infernal Mobs draws its name, bar and modifiers at the top of the screen, right where the boss bars and the Champions
 * health HUD go, so they draw over each other. This works out how far down the Infernal Mobs HUD has to move to sit
 * below them (applied by InfernalMobsClientMixin_HudOffset).
 */
@SideOnly(Side.CLIENT)
public final class InfernalHudOffset {

    private static final int VANILLA_FIRST_BAR_Y = 12; // where the first boss bar and the Infernal Mobs bar are drawn
    private static final int CHAMPIONS_HUD_BOTTOM = 38; // Champions: name y 12, bar y 21, affixes y 27, plus a gap
    private static final int INFERNAL_TOP = 10; // Infernal Mobs draws its name at y 10

    // Infernal Mobs draws before the boss bars in the same frame, so it uses where they ended last frame
    private static int bossBarsBottom = VANILLA_FIRST_BAR_Y;
    private static int lastBossBarsBottom = VANILLA_FIRST_BAR_Y;

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onPreOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.BOSSHEALTH) return;
        lastBossBarsBottom = bossBarsBottom;
        bossBarsBottom = VANILLA_FIRST_BAR_Y;
    }

    /** After every boss bar (also the ones PlayerBossBarHandler draws itself), the next one would go at y + increment */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onBossBar(RenderGameOverlayEvent.BossInfo event) {
        bossBarsBottom = Math.max(bossBarsBottom, event.getY() + event.getIncrement());
    }

    /** How far to move the Infernal Mobs HUD down for this mob, 0 if nothing is in the way */
    public static int getOffset(@Nullable Entity infernalTarget) {
        int offset = lastBossBarsBottom - VANILLA_FIRST_BAR_Y;
        if (infernalTarget != null && ModLoadedUtil.champions.isLoaded() && ChampionsUtil.showsHealthHud(infernalTarget)) {
            // Champions cancels the boss bars when it draws, so only its own HUD is in the way
            offset = Math.max(0, CHAMPIONS_HUD_BOTTOM + ChampionsUtil.hudYOffset() - INFERNAL_TOP);
        }
        return offset;
    }

    private InfernalHudOffset() {}
}
