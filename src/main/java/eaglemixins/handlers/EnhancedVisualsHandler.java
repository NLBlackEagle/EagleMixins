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
		
		//Radiation % to begin rendering low radiation
		@CreativeConfig
		public double renderThresholdLow = 0.001;
		//Radiation % to begin rendering medium radiation
		@CreativeConfig
		public double renderThresholdMedium = 0.05;
		//Radiation % to begin rendering high radiation
		@CreativeConfig
		public double renderThresholdHigh = 0.25;
		
		//Max rendering opacity of low radiation
		@CreativeConfig
		public double maxOpacityLow = 0.6;
		//Max rendering opacity of medium radiation
		@CreativeConfig
		public double maxOpacityMedium = 0.8;
		//Max rendering opacity of high radiation
		@CreativeConfig
		public double maxOpacityHigh = 1.0;
		
		//Max general rendering opacity
		@CreativeConfig
		public double maxOpacity = 0.8;
		
		//Opacity fade transition speed
		@CreativeConfig
		public double fadeFactor = 0.05;
		
		@CreativeConfig
		public VisualType radiation_low = new VisualTypeOverlay("radiation_low", 30);
		@CreativeConfig
		public VisualType radiation_med = new VisualTypeOverlay("radiation_med", 20);
		@CreativeConfig
		public VisualType radiation_high = new VisualTypeOverlay("radiation_high", 10);
		
		public Visual radiationLowVisual;
		public Visual radiationMedVisual;
		public Visual radiationHighVisual;
		
		@Override
		public void tick(@Nullable EntityPlayer player) {
			if(radiationLowVisual == null) {
				radiationLowVisual = new Visual(radiation_low, this, 0);
				radiationLowVisual.setOpacityInternal(0);
				VisualManager.add(radiationLowVisual);

				radiationMedVisual = new Visual(radiation_med, this, 0);
				radiationMedVisual.setOpacityInternal(0);
				VisualManager.add(radiationMedVisual);
				
				radiationHighVisual = new Visual(radiation_high, this, 0);
				radiationHighVisual.setOpacityInternal(0);
				VisualManager.add(radiationHighVisual);
			}
			
			double lowOpac = 0.0D;
			double medOpac = 0.0D;
			double highOpac = 0.0D;
			
			if(player != null) {
				double radPerc = 0.0D;
				if(NCConfig.radiation_enabled_public) {
					IEntityRads playerRads = RadiationHelper.getEntityRadiation(player);
					if(playerRads != null && !playerRads.isImmune()) {
						radPerc = playerRads.getRadiationLevel();
					}
				}
				
				if(radPerc > renderThresholdHigh) {
					lowOpac = maxOpacity;
					medOpac = maxOpacity;
					highOpac = maxOpacity * Math.min(1.0D, (radPerc - renderThresholdHigh) / Math.max(0.01D, (1.0D - renderThresholdHigh)));
				}
				else if(radPerc > renderThresholdMedium) {
					lowOpac = maxOpacity;
					medOpac = maxOpacity * Math.min(1.0D, (radPerc - renderThresholdMedium) / Math.max(0.01D, (renderThresholdHigh - renderThresholdMedium)));
				}
				else if(radPerc > renderThresholdLow) {
					lowOpac = maxOpacity * Math.min(1.0D, (radPerc - renderThresholdLow) / Math.max(0.01D, (renderThresholdMedium - renderThresholdLow)));
				}
			}
			
			lowOpac *= maxOpacityLow;
			medOpac *= maxOpacityMedium;
			highOpac *= maxOpacityHigh;
			
			if(radiationLowVisual.getOpacityInternal() < lowOpac)
				radiationLowVisual.setOpacityInternal((float)Math.min(radiationLowVisual.getOpacityInternal() + fadeFactor, lowOpac));
			else if(radiationLowVisual.getOpacityInternal() > lowOpac)
				radiationLowVisual.setOpacityInternal((float)Math.max(radiationLowVisual.getOpacityInternal() - fadeFactor, lowOpac));
			
			if(radiationMedVisual.getOpacityInternal() < medOpac)
				radiationMedVisual.setOpacityInternal((float)Math.min(radiationMedVisual.getOpacityInternal() + fadeFactor, medOpac));
			else if(radiationMedVisual.getOpacityInternal() > medOpac)
				radiationMedVisual.setOpacityInternal((float)Math.max(radiationMedVisual.getOpacityInternal() - fadeFactor, medOpac));
			
			if(radiationHighVisual.getOpacityInternal() < highOpac)
				radiationHighVisual.setOpacityInternal((float)Math.min(radiationHighVisual.getOpacityInternal() + fadeFactor, highOpac));
			else if(radiationHighVisual.getOpacityInternal() > highOpac)
				radiationHighVisual.setOpacityInternal((float)Math.max(radiationHighVisual.getOpacityInternal() - fadeFactor, highOpac));
		}
	}
}