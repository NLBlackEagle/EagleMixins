package eaglemixins;

import java.util.Map;

import fermiumbooter.FermiumRegistryAPI;
import net.minecraftforge.fml.common.Loader;
import org.spongepowered.asm.launch.MixinBootstrap;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.12.2")
public class EagleMixinsPlugin implements IFMLLoadingPlugin {

	public EagleMixinsPlugin() {
		MixinBootstrap.init();
		//False for Vanilla/Coremod mixins, true for regular mod mixins
		FermiumRegistryAPI.enqueueMixin(false, "mixins.eaglemixins.vanilla.json");
		FermiumRegistryAPI.enqueueMixin(true, "mixins.eaglemixins.rlartifacts.json", () -> Loader.isModLoaded("artifacts"));
		FermiumRegistryAPI.enqueueMixin(true, "mixins.eaglemixins.srparasites.json", () -> Loader.isModLoaded("srparasites"));
		FermiumRegistryAPI.enqueueMixin(true, "mixins.eaglemixins.fishsundeadrising.json", () -> Loader.isModLoaded("mod_lavacow"));
		FermiumRegistryAPI.enqueueMixin(true, "mixins.eaglemixins.loadingscreens.json", () -> Loader.isModLoaded("loadingscreens"));
		FermiumRegistryAPI.enqueueMixin(true, "mixins.eaglemixins.bettersurvival.json", () -> Loader.isModLoaded("mujmajnkraftsbettersurvival"));
		FermiumRegistryAPI.enqueueMixin(true, "mixins.eaglemixins.somanyenchantments.json", () -> Loader.isModLoaded("somanyenchantments"));
		FermiumRegistryAPI.enqueueMixin(true, "mixins.eaglemixins.cookingforblockheads.json", () -> Loader.isModLoaded("cookingforblockheads"));
		FermiumRegistryAPI.enqueueMixin(true, "mixins.eaglemixins.playerbosses.json", () -> Loader.isModLoaded("playerbosses"));
		FermiumRegistryAPI.enqueueMixin(true, "mixins.eaglemixins.dregorarl.json", () -> Loader.isModLoaded("drl"));
		FermiumRegistryAPI.enqueueMixin(true, "mixins.eaglemixins.otg.json", () -> Loader.isModLoaded("openterraingenerator"));
		FermiumRegistryAPI.enqueueMixin(true, "mixins.eaglemixins.vc.json", () -> Loader.isModLoaded("variedcommodities"));
	}

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[0];
	}
	
	@Override
	public String getModContainerClass()
	{
		return null;
	}
	
	@Override
	public String getSetupClass()
	{
		return null;
	}
	
	@Override
	public void injectData(Map<String, Object> data) { }
	
	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}