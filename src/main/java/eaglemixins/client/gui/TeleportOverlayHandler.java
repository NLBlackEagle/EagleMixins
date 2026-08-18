package eaglemixins.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TeleportOverlayHandler {

    private static long startTime = -1;
    private static final int MAX_DURATION = 25; // max 25 seconds (gets canceled early once arrived)
    private static final int MAX_DURATION_DIM = 10; // 10 seconds (only in dimension 3)
    private static boolean active = false;
    private static boolean isGlitch = false;

    public static void startRendering(boolean glitch) {
        startTime = System.currentTimeMillis();
        isGlitch = glitch;

        active = true;
        hidePlayer(true);
    }

    public static void stopRendering() {
        active = false;
        hidePlayer(false);
    }

    // Stops in 1.5 seconds
    public static void scheduleStop() {
        startTime = System.currentTimeMillis() - (isGlitch ? MAX_DURATION_DIM : MAX_DURATION)*1000 + 1500;
    }

    private static void hidePlayer(boolean shouldHide){
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null){
            player.setInvisible(shouldHide);
            player.noClip = shouldHide;
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (!active || event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc);
        int width = res.getScaledWidth();
        int height = res.getScaledHeight();

        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(0F, 0F, 0F, 1F);

        if (!isGlitch) drawRect(width, height, 0xFF000000);

        GlStateManager.enableTexture2D();

        // Draw centered title text
        String msg = isGlitch
                ? I18n.format("overlay.teleport.glitch")
                : I18n.format("overlay.teleport.normal");

        String[] lines = msg.split("\\\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            mc.fontRenderer.drawStringWithShadow(
                    line,
                    (float) width / 2 - (float) mc.fontRenderer.getStringWidth(line) / 2,
                    (float) height / 2 + ((float) mc.fontRenderer.FONT_HEIGHT + 4) * i,
                    0xFFFFFF
            );
        }

        GlStateManager.enableDepth();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.disableBlend();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!active || event.phase != TickEvent.Phase.END) return;

        long elapsed = System.currentTimeMillis() - startTime;
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.player.dimension == 3 && elapsed > MAX_DURATION_DIM * 1000L) {
            stopRendering();
        } else if (elapsed > MAX_DURATION * 1000L) {
            stopRendering();
        }
    }

    private static void drawRect(int right, int bottom, int color) {
        int a = (color >> 24) & 255;
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(0, bottom, 0).color(r, g, b, a).endVertex();
        buffer.pos(right, bottom, 0).color(r, g, b, a).endVertex();
        buffer.pos(right, 0, 0).color(r, g, b, a).endVertex();
        buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
        tessellator.draw();
    }
}
