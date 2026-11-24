package eaglemixins.mixin.bqstandard;

import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import bq_standard.handlers.LootSaveLoad;
import bq_standard.rewards.loot.LootRegistry;
import com.google.gson.JsonObject;
import eaglemixins.EagleMixins;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

/**
 * Standard Expansion loads a world's QuestLoot.json whenever it exists and only falls back to config/betterquesting/DefaultLoot.json
 * when it doesn't. A world whose QuestLoot.json got saved without any loot groups (started without the default file, groups deleted, ...)
 * then stays empty forever: every random loot chest, like the Sacrificial Amulet reward, opens as "No Loot Setup" and gives nothing.
 * When the loaded loot has no groups, this loads the default loot instead; the next world save writes it to QuestLoot.json.
 */
@Mixin(value = LootSaveLoad.class, remap = false)
public abstract class LootSaveLoadDefaultFallbackMixin {
    @Inject(method = "LoadLoot", at = @At("TAIL"))
    private void eaglemixins$fallBackToDefaultLoot(MinecraftServer server, CallbackInfo ci) {
        if (LootRegistry.INSTANCE.size() > 0) return;

        File defaultLoot = server.getFile("config/betterquesting/DefaultLoot.json");
        if (!defaultLoot.exists()) return;

        JsonObject json = JsonHelper.ReadFromFile(defaultLoot);
        LootRegistry.INSTANCE.readFromNBT(NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound(), true), false);
        if (LootRegistry.INSTANCE.size() > 0) {
            EagleMixins.LOGGER.warn("World loot database (QuestLoot.json) had no loot groups, loaded {} groups from DefaultLoot.json instead", LootRegistry.INSTANCE.size());
        }
    }
}
