package eaglemixins.mixin.qualitytools;

import com.tmtravlr.qualitytools.config.QualityItem;
import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import eaglemixins.util.LootTableSetter;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;

@Mixin(value = QualityItem.Serializer.class, remap = false)
public class QualityItemSerializerMixin {

    @Inject(method = "deserialize", at = @At("RETURN"))
    private void onDeserialize(JsonElement jsonElement,
                               Type type,
                               JsonDeserializationContext context,
                               CallbackInfoReturnable<QualityItem> cir) {

        if (jsonElement == null || !jsonElement.isJsonObject())
            return;

        QualityItem item = cir.getReturnValue();
        if (item == null)
            return;

        JsonObject json = jsonElement.getAsJsonObject();

        if (!json.has("loottable"))
            return;

        LootTableSetter setter = (LootTableSetter) item;
        JsonElement lootEl = json.get("loottable");

        if (lootEl.isJsonPrimitive()) {
            setter.eaglemixins$addLootTable(
                    new ResourceLocation(lootEl.getAsString())
            );
        }
        else if (lootEl.isJsonArray()) {
            for (JsonElement e : lootEl.getAsJsonArray()) {
                setter.eaglemixins$addLootTable(
                        new ResourceLocation(e.getAsString())
                );
            }
        }
    }
}