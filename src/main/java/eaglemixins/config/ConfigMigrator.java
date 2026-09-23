package eaglemixins.config;

import eaglemixins.EagleMixins;
import meldexun.betterconfig.api.ConfigMigrationHelper;
import meldexun.betterconfig.api.tree.*;
import org.apache.maven.artifact.versioning.ArtifactVersion;

public class ConfigMigrator {
    public static <T extends IConfigContext<T>> void handleMigration(IConfigCategory<T> general, T context, ArtifactVersion fileVersion) {
        // first creation of the file, no need to migrate
        if(general.getElements().isEmpty() && general.getSubCategories().isEmpty()) return;
        // migrate from pre Better Config to cfg version 1.0.0
        if (fileVersion == null) migrateTo1_0_0(general, context);

        // migrate to next:
        //if (fileVersion.containsVersion(new DefaultArtifactVersion("1.0.0"))) ...
    }

    private static <T extends IConfigContext<T>> void migrateTo1_0_0(IConfigCategory<T> general, T context) {
        try {
            IConfigCategory<T> nuclearcraftOptions = general.getSubCategories().get("nuclearcraft options");
            if (nuclearcraftOptions == null) return;

            migrateRadiationResistanceList(nuclearcraftOptions, context);
            ConfigMigrationHelper.renameCategory(nuclearcraftOptions, "radiating inventories", "Radiating Inventories");
            ConfigMigrationHelper.renameCategory(nuclearcraftOptions, "radiating loot tables", "Radiating Loot Tables");
        } catch (Exception e) {
            EagleMixins.LOGGER.error("Config migration failed", e);
            throw new RuntimeException("Config migration failed", e);
        }
    }

    private static <T extends IConfigContext<T>> void migrateRadiationResistanceList(IConfigCategory<T> nuclearcraftOptions, T context) {
        IConfigElement<T> el = nuclearcraftOptions.getElements().remove("RadiationResistanceList");
        if (!(el instanceof IConfigList)) return;
        IConfigList<T> oldRadiationResistanceList = (IConfigList<T>) el;

        IConfigCategory<T> newRadiationResistanceMap = context.createCategory();

        // Parse mobid=radres to map(=category) of mobid : radres
        for (IConfigElement<T> element : oldRadiationResistanceList.getList()) {
            if (!(element instanceof IConfigValue)) continue;
            String[] split = ((IConfigValue<T>) element).getValue().split("=");
            if (split.length != 2) continue;

            IConfigValue<T> radRes = context.createValue();
            radRes.setValue(split[1].trim());

            newRadiationResistanceMap.getElements().put(split[0].trim(), radRes);
        }
        nuclearcraftOptions.getSubCategories().put("RadiationResistanceList", newRadiationResistanceMap);
    }
}
