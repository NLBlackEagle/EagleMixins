package eaglemixins.mixin.otg;

import com.pg85.otg.forge.world.OTGWorldType;
import eaglemixins.config.ForgeConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;

@Mixin(OTGWorldType.class)
public abstract class OTGWorldTypeOverworldPresetMixin {

    // When a new world has no saved Dimensions config yet, OTG uses the world folder name (level-name) as the overworld preset name.
    // Every File.getName() call in getBiomeProvider is that lookup, so return the configured preset instead.
    @Redirect(
            method = "getBiomeProvider",
            at = @At(value = "INVOKE", target = "Ljava/io/File;getName()Ljava/lang/String;"),
            remap = false
    )
    private String eaglemixins_otgWorldType_getBiomeProvider(File worldDirectory) {
        String preset = ForgeConfigHandler.server.otgOverworldPreset;
        if (preset == null || preset.trim().isEmpty()) return worldDirectory.getName();
        return preset.trim();
    }
}
