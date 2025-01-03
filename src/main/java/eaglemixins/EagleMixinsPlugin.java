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