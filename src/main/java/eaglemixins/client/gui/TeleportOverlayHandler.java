package eaglemixins.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TeleportOverlayHandler {

    private static TeleportOverlayHandler INSTANCE;

    private static long startTime = -1;
    private static final int MAX_DURATION_TICKS = 500; // 25 seconds
    private static final int MAX_DURATION_TICKS_DIM = 200; // 10 seconds (only in dimension 3)
    private static boolean active = false;
    private static boolean glitchActive = false;


    public static void trigger(boolean glitch) {
        if (INSTANCE != null) MinecraftForge.EVENT_BUS.unregister(INSTANCE);

        INSTANCE = new TeleportOverlayHandler();
        startTime = System.currentTimeMillis();
        active = true;
        glitchActive = glitch;

        MinecraftForge.EVENT_BUS.register(INSTANCE);

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null) {
            player.setInvisible(true);
            player.capabilities.disableDamage = true;
            player.noClip = true;
            player.sendPlayerAbilities();
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
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

        if (!glitchActive) {
            drawRect(width, height);
        }

        // 🔥 FIX: Re-enable texture2D so font rendering works
        GlStateManager.enableTexture2D();

        // Draw centered title text
        String msg = glitchActive
                ? I18n.format("overlay.teleport.glitch")
                : I18n.format("overlay.teleport.normal");
        mc.fontRenderer.drawStringWithShadow(
                msg,
                (float) width / 2 - (float) mc.fontRenderer.getStringWidth(msg) / 2,
                (float) height / 2,
                0xFFFFFF
        );

        GlStateManager.enableDepth();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!active || event.phase != TickEvent.Phase.END) return;

        long elapsed = System.currentTimeMillis() - startTime;
        Minecraft mc = Minecraft.getMinecraft();

        if (elapsed > MAX_DURATION_TICKS_DIM * 50L && mc.player.dimension == 3) {
            cleanup();
        } else if (elapsed > MAX_DURATION_TICKS * 50L) {
            cleanup();
        }
    }


    private void cleanup() {

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null) {
            mc.player.setInvisible(false);
            mc.player.capabilities.disableDamage = false;
            mc.player.noClip = false;
            mc.player.sendPlayerAbilities();
        }

        glitchActive = false;
        active = false;
        MinecraftForge.EVENT_BUS.unregister(INSTANCE);
    }

    private void drawRect(int right, int bottom) {
        int a = (-16777216 >> 24) & 255;
        int r = (-16777216 >> 16) & 255;
        int g = (-16777216 >> 8) & 255;
        int b = -16777216 & 255;

        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(0, bottom, 0).color(r, g, b, a).endVertex();
        buffer.pos(right, bottom, 0).color(r, g, b, a).endVertex();
        buffer.pos(right, 0, 0).color(r, g, b, a).endVertex();
        buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
        tessellator.draw();
    }

    public static void stop() {
        active = false;
        if (INSTANCE != null) {
            MinecraftForge.EVENT_BUS.unregister(INSTANCE);
            INSTANCE = null;
        }

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null) {
            player.setInvisible(false);
            player.capabilities.disableDamage = false;
            player.noClip = false;
            player.sendPlayerAbilities();
        }
    }
}
