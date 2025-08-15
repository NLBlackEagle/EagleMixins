package eaglemixins.mixin.otg;

import com.pg85.otg.generator.biome.layers.Layer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Layer.class)
public abstract class LayerMixin {
    @Shadow(remap = false) protected abstract int nextInt(int x);

    /**
     * @author nischhelm + kotlin
     * @reason temp
     */
    @Overwrite(remap = false)
    protected int getRandomOf4(int a, int b, int c, int d) {
        if(b == c && c == d) return b; // b = c = d, a different
        if(a == b && a == c) return a; // a = b = c, d different
        if(a == b && a == d) return a; // a = b = d, c different
        if(a == c && a == d) return a; // a = c = d, b different
        if(a == b && c != d) return a; //
        if(a == c && b != d) return a;
        if(a == d && b != c) return a;
        if(b == c && a != d) return b;
        if(b == d && a != c) return b;
        if(c == d && a != b) return c;

        switch (this.nextInt(4)){
            case 0: return a;
            case 1: return b;
            case 2: return c;
            case 3: default: return d;
        }
    }
}