package eaglemixins.mixin.qualitytools;

import com.tmtravlr.qualitytools.config.QualityItem;
import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import eaglemixins.util.LootTableSetter;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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

        if (json.has("loottable")) {
            JsonElement lootEl = json.get("loottable");

            if (lootEl.isJsonPrimitive()) {
                ((LootTableSetter) item).eaglemixins$setLootTable(
                        new ResourceLocation(lootEl.getAsString())
                );
                System.out.println("[DEBUG] contains loottable: " + lootEl.getAsString());
            } else if (lootEl.isJsonArray()) {
                List<ResourceLocation> lootTables = new ArrayList<>();
                for (JsonElement e : lootEl.getAsJsonArray()) {
                    lootTables.add(new ResourceLocation(e.getAsString()));
                    System.out.println("[DEBUG] contains loottable: " + e.getAsString());
                }
                ((LootTableSetter) item).eaglemixins$setLootTables(lootTables);
            }
        }
    }
}