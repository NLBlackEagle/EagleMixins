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
		
		//Low rad render minimum
		@CreativeConfig
		public double lowRadRender = 0.2;
		//Med rad render minimum
		@CreativeConfig
		public double medRadRender = 0.5;
		//High rad render minimum
		@CreativeConfig
		public double highRadRender = 0.8;
		//Render opacity mult
		@CreativeConfig
		public double renderOpacityMult = 0.75;
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
						radPerc = playerRads.getRadsPercentage() / 100.0D;
					}
				}
				
				if(radPerc > highRadRender) {
					lowOpac = renderOpacityMult;
					medOpac = renderOpacityMult;
					highOpac = renderOpacityMult * Math.min(1.0D, (radPerc - highRadRender) / Math.max(0.01D, (1.0D - highRadRender)));
				}
				else if(radPerc > medRadRender) {
					lowOpac = renderOpacityMult;
					medOpac = renderOpacityMult * Math.min(1.0D, (radPerc - medRadRender) / Math.max(0.01D, (highRadRender - medRadRender)));
				}
				else if(radPerc > lowRadRender) {
					lowOpac = renderOpacityMult * Math.min(1.0D, (radPerc - lowRadRender) / Math.max(0.01D, (medRadRender - lowRadRender)));
				}
			}
			
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