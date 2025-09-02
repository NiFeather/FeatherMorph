package xyz.nifeather.morph.storage.skill;

import org.apache.commons.io.FileUtils;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xyz.nifeather.morph.abilities.impl.AttributeModifyingAbility;
import xyz.nifeather.morph.abilities.options.AttributeModifyOption;
import xyz.nifeather.morph.abilities.options.ExtraAirOption;
import xyz.nifeather.morph.abilities.options.PotionEffectOption;
import xyz.nifeather.morph.abilities.options.ReduceDamageOption;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.skills.DefaultConfigGenerator;
import xyz.nifeather.morph.storage.DirectoryJsonBasedStorage;
import xyz.nifeather.morph.storage.MorphJsonBasedStorage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SkillsConfigurationStoreNew extends DirectoryJsonBasedStorage<SkillAbilityConfigContainer>
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

        if (packageVersion > TARGET_PACKAGE_VERSION)
            logger.warn("The package version is newer than our implementation! Errors may occur!");
    }

    private static final int TARGET_PACKAGE_VERSION = PackageVersions.POTION_MIGRATE;

    private void update(int currentVersion)
    {
        if (currentVersion < PackageVersions.INITIAL)
        {
            var legacySkillFile = new File(this.plugin.getDataFolder(), "skills.json");

            if (legacySkillFile.exists())
                migrateFromLegacyStorage();
            else
                saveDefaultGeneratedConfigurations();
        }

        if (currentVersion < PackageVersions.ATTRIBUTE_NAME_CHANGED)
            migrateAttribute();

        if (currentVersion < PackageVersions.WITHER_SKELETON_CHANGES)
            migrateWitherSkeleton();

        if (currentVersion < PackageVersions.MERGE_ATTRIBUTE_AGAIN)
        {
            logger.info("Migrating attribute name again, to fix windows migrate issue.");
            migrateAttribute();
        }

        var generatedConfigurations = DefaultConfigGenerator.createInstance().generateConfiguration();

        if (currentVersion < PackageVersions.HAPPY_GHAST)
        {
            saveEntityTypeConfiguration(generatedConfigurations, EntityType.HAPPY_GHAST);
        }

        if (currentVersion < PackageVersions.GUARDIAN_SKILL)
        {
            saveEntityTypeConfiguration(generatedConfigurations, EntityType.GUARDIAN);
        }

        if (currentVersion < PackageVersions.EXTRA_AIR_ABILITY)
        {
            migrateMaxAirOption(EntityType.AXOLOTL, 6000); // See NMS Axolotl#getDefaultMaxAirSupply
            migrateMaxAirOption(EntityType.DOLPHIN, 4800); // See NMS Dolphin#getDefaultMaxAirSupply
        }

        if (currentVersion < PackageVersions.POTION_MIGRATE)
        {
            migratePotionEffect();
        }

        setPackageVersion(TARGET_PACKAGE_VERSION);
    }

    private void migratePotionEffect()
    {
        var files = directoryStorage.getFiles(".*\\.json$");
        for (File file : files)
        {
            var config = loadFrom(file);
            if (config == null)
            {
                logger.warn("Can't load SkillAbilityConfiguration from '%s', see errors above.".formatted(file.toString()));
                continue;
            }

            var key = getKeyFromFile(file);
            if (key == null)
            {
                logger.warn("Can't get mob key from '%s', see errors above.".formatted(file.toString()));
                continue;
            }

            if (config.getAbilitiyIdentifiers().contains(AbilityNames.POTION_ON_ATTACK.asString()))
            {
                config.legacy_MobID = key;
                logger.info("Migrating " + key);

                try
                {
                    var option = config.readOptions(AbilityNames.POTION_ON_ATTACK, PotionEffectOption.LEGACY_OPTION_HANDLER);
                    config.setOption(AbilityNames.POTION_ON_ATTACK, PotionEffectOption.OPTION_HANDLER, option);

                    save(config);
                    logger.info("Done saving new potion id for {}", file.getName());
                }
                catch (ParseErrorException | NullPointerException e)
                {
                    logger.error("Unable to read legacy potion option, ignoring...", e);
                }
            }
        }
    }

    private void migrateMaxAirOption(EntityType type, int air)
    {
        logger.info("Migrating %s configuration...".formatted(type));

        var configuration = this.get(type.key().asString());
        if (configuration == null)
        {
            logger.info("No configuration present, skipping...");
            return;
        }

        configuration.addAbility(AbilityNames.EXTRA_AIR)
                .appendOption(AbilityNames.EXTRA_AIR,
                        ExtraAirOption.OPTION_HANDLER,
                        new ExtraAirOption(air));

        configuration.legacy_MobID = type.key().asString();
        this.save(configuration);

        logger.info("Done Migrating new %s configuration".formatted(type));
    }

    private void saveEntityTypeConfiguration(Map<String, SkillAbilityConfigContainer> defaultConfigurations, EntityType entityType)
    {
        var newConfig = defaultConfigurations.getOrDefault(entityType.key().asString(), null);

        if (newConfig == null)
            return;

        newConfig.legacy_MobID = entityType.key().asString();

        save(newConfig);
    }

    private void migrateAttribute()
    {
        logger.info("Starting migration of attribute names...");
        var files = directoryStorage.getFiles(".*\\.json$");

        var abilityInstance = new AttributeModifyingAbility();

        for (File file : files)
        {
            var config = this.loadFrom(file);
            if (config == null)
            {
                logger.warn("Can't load SkillAbilityConfiguration from '%s', see errors above.".formatted(file.toString()));
                continue;
            }

            AttributeModifyOption option;

            try
            {
                if (config.getAbilitiyIdentifiers().contains(abilityInstance.getIdentifier().asString()))
                    option = config.readOptions(abilityInstance);
                else
                    option = null;
            }
            catch (ParseErrorException | NullPointerException e)
            {
                logger.error("Can't read ability option from file! Skipping {}", file.getName(), e);
                continue;
            }

            if (option == null) continue;

            var key = getKeyFromFile(file);
            if (key == null)
            {
                logger.warn("Can't get mob key from '%s', see errors above.".formatted(file.toString()));
                continue;
            }

            config.legacy_MobID = key;
            logger.info("Migrating " + key);

            for (AttributeModifyOption.AttributeInfo attributeInfo : option.modifiers)
            {
                if (!attributeInfo.isValid())
                {
                    logger.warn("Invalid attribute info for '%s > %s'! Ignoring...".formatted(
                            config.legacy_MobID,
                            attributeInfo.attributeName == null ? "<unknown attribute name>" : attributeInfo.attributeName));

                    continue;
                }

                attributeInfo.attributeName = attributeInfo.attributeName.replace("generic.", "");
            }

            config.appendOption(abilityInstance.getIdentifier(), abilityInstance.optionHandler(), option);

            this.save(config);
        }

        logger.info("Done.");
    }

    private void migrateFromLegacyStorage()
    {
        try
        {
            logger.info("Migrating from legacy skill configuration...");
            var storage = new LegacyReadonlyConfigurationStorage();

            storage.initializeStorage();

            var file = storage.file();

            var storing = storage.getStoring();
            if (storing == null)
            {
                logger.warn("Can't migrate from legacy skill configuration: Null storing object, is everything all right?");
                return;
            }

            storing.configurations.forEach(this::save);

            var success = file.renameTo(new File(file.getParent(), "skills.json.old"));

            if (!success)
                logger.info("Can't rename 'skills.json' to 'skills.json.old', but it's not a big deal, I guess...");

            logger.info("Done migrating legacy skill configuration!");
        }
        catch (Throwable t)
        {
            logger.warn("Can't migrate from legacy skill configuration", t);
        }
    }

    private void migrateWitherSkeleton()
    {
        logger.info("Migrating new Wither Skeleton configuration");

        var configuration = this.get(EntityType.WITHER_SKELETON.key().asString());
        if (configuration == null)
        {
            logger.info("No configuration present for minecraft:wither_skeleton, skipping...");
            return;
        }

        configuration.addAbility(AbilityNames.HAS_FIRE_RESISTANCE)
                .addAbility(AbilityNames.REDUCES_WITHER_DAMAGE)
                .appendOption(AbilityNames.REDUCES_WITHER_DAMAGE,
                        ReduceDamageOption.OPTION_HANDLER,
                        new ReduceDamageOption(1, true));

        configuration.legacy_MobID = EntityType.WITHER_SKELETON.key().asString();
        this.save(configuration);

        logger.info("Done Migrating new Wither Skeleton configuration");
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

    public void save(SkillAbilityConfigContainer configuration)
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
            logger.error("Can't write content to file", t);
        }
    }

    private static final SkillAbilityConfigContainer defaultConfig = new SkillAbilityConfigContainer();

    @Override
    protected SkillAbilityConfigContainer getDefault()
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
        public static final int ATTRIBUTE_NAME_CHANGED = 2;
        public static final int WITHER_SKELETON_CHANGES = 3;
        public static final int MERGE_ATTRIBUTE_AGAIN = 4;
        public static final int HAPPY_GHAST = 5;
        public static final int GUARDIAN_SKILL = 6;
        public static final int EXTRA_AIR_ABILITY = 7;
        public static final int POTION_MIGRATE = 8;
    }
}
