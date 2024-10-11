package xyz.nifeather.morph.storage.skill;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xyz.nifeather.morph.skills.DefaultConfigGenerator;
import xyz.nifeather.morph.storage.DirectoryJsonBasedStorage;
import xyz.nifeather.morph.storage.MorphJsonBasedStorage;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class SkillsConfigurationStoreNew extends DirectoryJsonBasedStorage<SkillAbilityConfiguration>
{
    public SkillsConfigurationStoreNew()
    {
        super("skills");
    }

    @Initializer
    private void load()
    {
        var packageVersion = this.getPackageVersion();

        if (packageVersion < TARGET_PACKAGE_VERSION)
            update(packageVersion);
    }

    private static final int TARGET_PACKAGE_VERSION = PackageVersions.INITIAL;

    private void update(int currentVersion)
    {
        if (currentVersion < PackageVersions.INITIAL)
        {
            URI dataFolderUri = this.plugin.getDataFolder().toURI();
            var legacySkillFile = new File(URI.create(dataFolderUri + "/" + "skills.json"));

            if (legacySkillFile.exists())
                migrateFromLegacyStorage();
            else
                saveDefaultGeneratedConfigurations();
        }

        setPackageVersion(TARGET_PACKAGE_VERSION);
    }

    private void migrateFromLegacyStorage()
    {
        try
        {
            logger.info("Migrating from legacy skill configuration...");
            var storage = new LegacyReadonlyConfigurationStorage();
            var file = storage.file();

            if (!file.exists())
            {
                logger.info("Legacy skill configuration doesn't exist, ignoring...");
                return;
            }

            storage.initializeStorage();

            var storing = storage.getStoring();
            if (storing == null)
            {
                logger.warn("Can't migrate from legacy skill configuration: Null storing object, is everything all right?");
                return;
            }

            storing.configurations.forEach(this::save);

            var success = file.renameTo(new File(file.getParent(), "skills.old.json"));

            if (!success)
                logger.info("Can't rename 'skills.json' to 'skills.old.json', but it's not a big deal, I guess...");

            logger.info("Done migrating legacy skill configuration!");
        }
        catch (Throwable t)
        {
            logger.warn("Can't migrate from legacy skill configuration: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private void saveDefaultGeneratedConfigurations()
    {
        logger.info("Saving default generated skill configurations...");

        var generatedConfiguration = DefaultConfigGenerator.createInstance().generateConfiguration();
        generatedConfiguration.forEach((id, config) ->
        {
            config.legacy_MobID = id;
            this.save(config);
        });

        logger.info("Done saving default generated skill configurations!");
    }

    public void save(SkillAbilityConfiguration configuration)
    {
        var identifier = configuration.legacy_MobID;

        if (identifier == null)
        {
            logger.warn("Found a configuration from legacy store that doesn't have a mobId! Ignoring...");
            return;
        }

        var path = this.getPath(identifier) + ".json";

        var file = this.directoryStorage.getFile(path, true);
        if (file == null)
        {
            logger.warn("Cannot save disguise configuration for " + identifier);
            return;
        }

        String json = gson.toJson(configuration);
        try
        {
            FileUtils.writeStringToFile(file, json, StandardCharsets.UTF_8);
        }
        catch (Throwable t)
        {
            logger.error("Can't write content to file: " + t.getMessage());
        }
    }

    private static final SkillAbilityConfiguration defaultConfig = new SkillAbilityConfiguration();

    @Override
    protected SkillAbilityConfiguration getDefault()
    {
        return defaultConfig;
    }

    private static class LegacyReadonlyConfigurationStorage extends MorphJsonBasedStorage<SkillAbilityConfigurationContainer>
    {
        @Override
        protected @NotNull String getFileName()
        {
            return "skills.json";
        }

        public File file()
        {
            return this.configurationFile;
        }

        @Nullable
        public SkillAbilityConfigurationContainer getStoring()
        {
            return this.storingObject;
        }

        @Override
        protected @NotNull SkillAbilityConfigurationContainer createDefault()
        {
            return new SkillAbilityConfigurationContainer();
        }

        @Override
        protected @NotNull String getDisplayName()
        {
            return "Legacy skill configuration store";
        }
    }

    public static class PackageVersions
    {
        public static final int INITIAL = 1;
    }
}
