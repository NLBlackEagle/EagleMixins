package eaglemixins.mixin.otg.missingbiomecrashfix;

import com.llamalad7.mixinextras.sugar.Local;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.network.ServerConfigProvider;
import com.pg85.otg.worldsave.BiomeIdData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(ServerConfigProvider.class)
public abstract class ServerConfigProviderMixin {

    @Inject(
            method = "indexSettings",
            at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 2),
            remap = false
    )
    private void eaglemixins_otgLayerFactory_initMainLayer(CallbackInfoReturnable<String> cir, @Local(name = "loadedBiomeList") List<BiomeConfig> loadedBiomeList, @Local(name = "loadedBiomeIdData") ArrayList<BiomeIdData> loadedBiomeIdData, @Local(name = "usedBiomes") List<BiomeConfig> usedBiomes){
        if(loadedBiomeIdData == null) return;

        Set<String> oldBiomes = loadedBiomeIdData.stream()
                .map(id -> id.biomeName)
                .collect(Collectors.toSet());

        loadedBiomeList.stream()
                .filter(biome -> !oldBiomes.contains(biome.getName()))
                .forEach(usedBiomes::add);
    }
}
