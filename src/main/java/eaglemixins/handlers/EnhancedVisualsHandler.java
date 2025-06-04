package eaglemixins.handlers;

import com.creativemd.creativecore.common.config.api.CreativeConfig;
import eaglemixins.EagleMixins;
import nc.capability.radiation.entity.IEntityRads;
import nc.config.NCConfig;
import nc.radiation.RadiationHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import team.creative.enhancedvisuals.api.Visual;
import team.creative.enhancedvisuals.api.VisualHandler;
import team.creative.enhancedvisuals.api.type.VisualType;
import team.creative.enhancedvisuals.api.type.VisualTypeOverlay;
import team.creative.enhancedvisuals.client.VisualManager;
import team.creative.enhancedvisuals.common.visual.VisualRegistry;

import javax.annotation.Nullable;

public class EnhancedVisualsHandler {
	
	public static RadiationHandler RADIATION;
	
	public static void init() {
		VisualRegistry.registerHandler(new ResourceLocation(EagleMixins.MODID, "radiation"), RADIATION = new RadiationHandler());
	}
	
	public static class RadiationHandler extends VisualHandler {
		
		//What % rads is considered the starting point for rendering
		@CreativeConfig
		public double minRadRender = 0.2;
		//What % rads is considered the max point for rendering
		@CreativeConfig
		public double maxRadRender = 1.0;
		//Max render opacity mult
		@CreativeConfig
		public double maxRenderOpacity = 0.5;
		//Opacity fade transition speed
		@CreativeConfig
		public double fadeFactor = 0.005;
		
		@CreativeConfig
		public VisualType radiation = new VisualTypeOverlay("radiation", 40);
		
		public Visual radiationVisual;
		
		@Override
		public void tick(@Nullable EntityPlayer player) {
			if(radiationVisual == null) {
				radiationVisual = new Visual(radiation, this, 0);
				radiationVisual.setOpacityInternal(0);
				VisualManager.add(radiationVisual);
			}
			if(player != null) {
				double aimedRads = 0;
				if(NCConfig.radiation_enabled_public) {
					IEntityRads playerRads = RadiationHelper.getEntityRadiation(player);
					if(playerRads != null && !playerRads.isImmune()) {
						aimedRads = playerRads.getRadsPercentage() / 100.0D;
					}
				}
				aimedRads = (aimedRads - minRadRender) / maxRadRender;
				aimedRads = Math.max(0.0D, Math.min(aimedRads, 1.0D) * maxRenderOpacity);
				
				if(radiationVisual.getOpacityInternal() < aimedRads) {
					radiationVisual.setOpacityInternal((float)Math.min(radiationVisual.getOpacityInternal() + fadeFactor, aimedRads));
				}
				else if(radiationVisual.getOpacityInternal() > aimedRads) {
					radiationVisual.setOpacityInternal((float)Math.max(radiationVisual.getOpacityInternal() - fadeFactor, aimedRads));
				}
			}
		}
	}
}