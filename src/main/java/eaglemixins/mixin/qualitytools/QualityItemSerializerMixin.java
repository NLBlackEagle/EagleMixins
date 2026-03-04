package eaglemixins.mixin.qualitytools;

import com.tmtravlr.qualitytools.config.QualityItem;
import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;

@Mixin(value = QualityItem.Serializer.class, remap = false)
public class QualityItemSerializerMixin {

    @Inject(method = "deserialize", at = @At("RETURN"))
    private void onDeserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context, CallbackInfoReturnable<QualityItem> cir) {
        // Only debug, no changes
        if (jsonElement == null || !jsonElement.isJsonObject()) return;

        JsonObject json = jsonElement.getAsJsonObject();

        // Check for "loottable" field
        if (json.has("loottable")) {
            JsonElement lootEl = json.get("loottable");
            if (lootEl.isJsonPrimitive()) {
                System.out.println("[DEBUG] contains loottable: " + lootEl.getAsString());
            } else if (lootEl.isJsonArray()) {
                JsonArray array = lootEl.getAsJsonArray();
                for (JsonElement e : array) {
                    if (e.isJsonPrimitive()) {
                        System.out.println("[DEBUG] contains loottable: " + e.getAsString());
                    }
                }
            }
        }
    }
}