package eaglemixins.client.render;

import eaglemixins.entity.EntityPlayerBoss;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

/** Player model with the boss' skin (classic or slim arms), armor, held items, and xat dragon wings / horns. */
@SideOnly(Side.CLIENT)
public class RenderPlayerBoss extends RenderLivingBase<EntityPlayerBoss> {

    private static final Map<String, ResourceLocation> SKINS = new HashMap<>();

    private final ModelPlayer classicModel = new ModelPlayer(0.0F, false);
    private final ModelPlayer slimModel = new ModelPlayer(0.0F, true);

    public RenderPlayerBoss(RenderManager manager) {
        super(manager, new ModelPlayer(0.0F, false), 0.5F);
        mainModel = classicModel;
        addLayer(new LayerBipedArmor(this));
        addLayer(new LayerHeldItem(this));
        addLayer(new LayerArrow(this));
        addLayer(new LayerCustomHead(classicModel.bipedHead));
        addLayer(new LayerWings());
    }

    @Override
    public void doRender(EntityPlayerBoss entity, double x, double y, double z, float entityYaw, float partialTicks) {
        ModelPlayer model = entity.getRenderData().getBoolean("Slim") ? slimModel : classicModel;
        mainModel = model;

        ModelBiped.ArmPose mainPose = entity.getHeldItemMainhand().isEmpty() ? ModelBiped.ArmPose.EMPTY : ModelBiped.ArmPose.ITEM;
        ModelBiped.ArmPose offPose = entity.getHeldItemOffhand().isEmpty() ? ModelBiped.ArmPose.EMPTY : ModelBiped.ArmPose.ITEM;
        ItemStack main = entity.getHeldItemMainhand();
        if (entity.isSwingingArms() && main.getItem() instanceof ItemBow) mainPose = ModelBiped.ArmPose.BOW_AND_ARROW;
        if (entity.getPrimaryHand() == EnumHandSide.RIGHT) {
            model.rightArmPose = mainPose;
            model.leftArmPose = offPose;
        } else {
            model.rightArmPose = offPose;
            model.leftArmPose = mainPose;
        }
        model.isSneak = entity.isSneaking();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    /** Name tag with the subtitle as a second line under it, both centered like vanilla labels. */
    @Override
    protected void renderEntityName(EntityPlayerBoss entity, double x, double y, double z, String name, double distanceSq) {
        String subtitle = entity.getRenderData().getString("Subtitle");
        if (subtitle.isEmpty()) {
            super.renderEntityName(entity, x, y, z, name, distanceSq);
            return;
        }
        renderLivingLabel(entity, name, x, y + 0.27D, z, 64); // one label line (10px at 0.027 scale) up, so the subtitle sits where the name normally is
        renderLivingLabel(entity, subtitle, x, y, z, 64);
    }

    @Override
    protected void preRenderCallback(EntityPlayerBoss entity, float partialTicks) {
        float scale = 0.9375F * entity.getRenderScale(); // players render at 0.9375
        GlStateManager.scale(scale, scale, scale);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPlayerBoss entity) {
        String skin = entity.getRenderData().getString("Skin");
        if (skin.isEmpty()) return DefaultPlayerSkin.getDefaultSkinLegacy();
        return SKINS.computeIfAbsent(skin, s -> s.contains(":") ? new ResourceLocation(s) : new ResourceLocation("eaglemixins", "textures/entity/player_boss/" + s + ".png"));
    }

    private class LayerWings implements LayerRenderer<EntityPlayerBoss> {
        @Override
        public void doRenderLayer(EntityPlayerBoss entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            NBTTagCompound render = entity.getRenderData();
            boolean slim = render.getBoolean("Slim");
            if (render.getBoolean("Wings")) {
                XatRaceRenderCompat.renderWings(entity, RenderPlayerBoss.this, slim, partialTicks, scale, render.getInteger("WingPrimary"), render.getInteger("WingSecondary"));
            }
            XatRaceRenderCompat.renderHorns(entity, RenderPlayerBoss.this, slim, partialTicks, scale, render.getString("Horns"), render.getInteger("HornColor"));
        }

        @Override
        public boolean shouldCombineTextures() {
            return false;
        }
    }
}
