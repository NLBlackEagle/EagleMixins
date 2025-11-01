package eaglemixins;

import fermiumbooter.FermiumRegistryAPI;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
public class EagleMixinsPlugin implements IFMLLoadingPlugin {

	public EagleMixinsPlugin() {
		MixinBootstrap.init();

		// Temporary FUR 1.4.2 fixes because we are not using 1.5.0+
		FermiumRegistryAPI.enqueueMixin(true, "mixins.eaglemixins.furold.json", () -> Loader.isModLoaded("mod_lavacow") && Loader.instance().getIndexedModList().get("mod_lavacow").getVersion().equals("1.4.2"));

		//Debug stuff
		FermiumRegistryAPI.enqueueMixin(false, "mixins.eaglemixins.debug.vanilla.json", EagleMixins.debugEnabled);
		FermiumRegistryAPI.enqueueMixin(true, "mixins.eaglemixins.debug.otg.json", () -> EagleMixins.debugEnabled && Loader.isModLoaded("openterraingenerator"));
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