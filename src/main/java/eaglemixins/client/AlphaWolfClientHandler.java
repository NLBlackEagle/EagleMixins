package eaglemixins.client;

import eaglemixins.config.ForgeConfigHandler;
import eaglemixins.config.folders.WolfPackConfig;
import eaglemixins.handlers.WolfPackHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/** Client side of alpha wolves: the alpha tag (from {@link eaglemixins.network.PacketSyncAlphaWolf}) and their skin */
public class AlphaWolfClientHandler {

    private static final ResourceLocation ALPHA_TEXTURE = new ResourceLocation("eaglemixins", "textures/entity/wolf/alpha_wolf.png");
    private static final ResourceLocation ALPHA_STALKING_TEXTURE = new ResourceLocation("eaglemixins", "textures/entity/wolf/alpha_wolf_stalking.png");
    private static final ResourceLocation ALPHA_ANGRY_TEXTURE = new ResourceLocation("eaglemixins", "textures/entity/wolf/alpha_wolf_angry.png");

    public static void markAlpha(int entityId, boolean stalking) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) return;
        Entity entity = mc.world.getEntityByID(entityId);
        if (entity == null) return;
        entity.addTag(WolfPackHandler.ALPHA_TAG);
        if (stalking) entity.addTag(WolfPackHandler.STALKING_TAG);
        else entity.removeTag(WolfPackHandler.STALKING_TAG);
    }

    /** Alpha skin for this wolf, or null to keep the vanilla one */
    @Nullable
    public static ResourceLocation getAlphaTexture(EntityWolf wolf) {
        WolfPackConfig cfg = ForgeConfigHandler.wolfpack;
        if (!cfg.enabled || !cfg.alphaCustomSkin || wolf.isTamed() || !WolfPackHandler.isAlpha(wolf)) return null;
        if (wolf.isAngry()) return ALPHA_ANGRY_TEXTURE;
        return wolf.getTags().contains(WolfPackHandler.STALKING_TAG) ? ALPHA_STALKING_TEXTURE : ALPHA_TEXTURE;
    }
}
