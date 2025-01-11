package eaglemixins.mixin.vanilla;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.JsonUtils;
import net.minecraft.world.storage.loot.RandomValueRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;

@Mixin(RandomValueRange.Serializer.class)
public abstract class RandomValueRangeMixin {
    @Inject(
            method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/world/storage/loot/RandomValueRange;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/loot/RandomValueRange;<init>(FF)V"),
            cancellable = true
    )
    void fixEagleMath_minGreaterMax(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_, CallbackInfoReturnable<RandomValueRange> cir, @Local JsonObject jsonobject){
        float min = JsonUtils.getFloat(jsonobject, "min");
        float max = JsonUtils.getFloat(jsonobject, "max");

        if(min > max) {
            cir.setReturnValue(new RandomValueRange(max, min));
        }
        //Else default behavior
        // --> return new RandomValueRange(min, max);
    }
}
