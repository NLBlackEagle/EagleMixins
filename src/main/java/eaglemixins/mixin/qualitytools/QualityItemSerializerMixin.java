package eaglemixins.mixin.qualitytools;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import com.tmtravlr.qualitytools.config.QualityItem;
import eaglemixins.util.LootTableSetter;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;

@Mixin(QualityItem.Serializer.class)
public class QualityItemSerializerMixin {

    @Inject(
            method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lcom/tmtravlr/qualitytools/config/QualityItem;",
            at = @At("TAIL"),
            remap = false
    )
    private void eaglemixins_deserializeLootTable(JsonElement jsonElement, Type type, JsonDeserializationContext context,
                               CallbackInfoReturnable<QualityItem> cir,
                               @Local(name = "qualityItem") QualityItem item,
                               @Local(name = "json") JsonObject json
    ) {
        if (!json.has("loottable")) return;

        LootTableSetter setter = (LootTableSetter) item;
        JsonElement lootEl = json.get("loottable");

        if (lootEl.isJsonPrimitive())
            setter.eaglemixins$setOrAddLootTable(new ResourceLocation(lootEl.getAsString()));
        else if (lootEl.isJsonArray()) {
            for (JsonElement e : lootEl.getAsJsonArray())
                setter.eaglemixins$setOrAddLootTable(new ResourceLocation(e.getAsString()));
        }
    }
}