package eaglemixins.client.gui;

import eaglemixins.entity.EntityPlayerBoss;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.BossInfoClient;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.BossInfo;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/** Draws the boss bar of player bosses that have a subtitle as: name, subtitle, bar, all centered. */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = "eaglemixins", value = Side.CLIENT)
public final class PlayerBossBarHandler {

    private static final ResourceLocation BARS = new ResourceLocation("textures/gui/bars.png");
    private static final int BAR_WIDTH = 182;

    @SubscribeEvent
    public static void onBossBar(RenderGameOverlayEvent.BossInfo event) {
        BossInfoClient info = event.getBossInfo();
        String subtitle = findSubtitle(info);
        if (subtitle == null || subtitle.isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer font = mc.fontRenderer;
        int centerX = event.getResolution().getScaledWidth() / 2;
        int y = event.getY(); // vanilla draws the name 9px above this and the bar at it

        event.setCanceled(true); // we draw name, subtitle and bar ourselves
        String name = info.getName().getFormattedText();
        font.drawStringWithShadow(name, centerX - font.getStringWidth(name) / 2.0F, y - 9, 0xFFFFFF);
        font.drawStringWithShadow(subtitle, centerX - font.getStringWidth(subtitle) / 2.0F, y + 1, 0xFFFFFF);
        drawBar(mc, centerX - BAR_WIDTH / 2, y + 11, info);
        event.setIncrement(event.getIncrement() + 11); // push the next boss bar down by the extra line
    }

    @Nullable
    private static String findSubtitle(BossInfoClient info) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) return null;
        String id = info.getUniqueId().toString();
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityPlayerBoss && id.equals(((EntityPlayerBoss) entity).getRenderData().getString("BossBar"))) {
                return ((EntityPlayerBoss) entity).getRenderData().getString("Subtitle");
            }
        }
        return null;
    }

    /** Same layout as the vanilla boss bar: background, overlay notches, then the filled part. */
    private static void drawBar(Minecraft mc, int x, int y, BossInfo info) {
        Gui gui = mc.ingameGUI;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BARS);
        int colorV = info.getColor().ordinal() * 10;
        int overlayV = 80 + (info.getOverlay().ordinal() - 1) * 10;
        boolean notched = info.getOverlay() != BossInfo.Overlay.PROGRESS;
        gui.drawTexturedModalRect(x, y, 0, colorV, BAR_WIDTH, 5);
        if (notched) gui.drawTexturedModalRect(x, y, 0, overlayV, BAR_WIDTH, 5);
        int filled = (int) (info.getPercent() * (BAR_WIDTH + 1));
        if (filled > 0) {
            gui.drawTexturedModalRect(x, y, 0, colorV + 5, filled, 5);
            if (notched) gui.drawTexturedModalRect(x, y, 0, overlayV + 5, filled, 5);
        }
    }

    private PlayerBossBarHandler() {}
}
