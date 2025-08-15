package eaglemixins.mixin.otg;

import com.pg85.otg.generator.biome.layers.Layer;
import com.pg85.otg.generator.biome.layers.LayerZoomFuzzy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LayerZoomFuzzy.class)
public abstract class LayerZoomFuzzyMixin extends Layer {
    /**
     * @author nischhelm + kotlin
     * @reason temp
     */
    @Overwrite(remap = false)
    protected int getRandomOf4(int a, int b, int c, int d) {
        switch (this.nextInt(4)){
            case 0: return a;
            case 1: return b;
            case 2: return c;
            case 3: default: return d;
        }
    }
}