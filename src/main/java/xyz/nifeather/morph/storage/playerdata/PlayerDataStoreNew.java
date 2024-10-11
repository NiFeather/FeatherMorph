package xyz.nifeather.morph.storage.playerdata;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.io.FileUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xyz.nifeather.morph.interfaces.IManagePlayerData;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.DisguiseTypes;
import xyz.nifeather.morph.storage.DirectoryJsonBasedStorage;
import xyz.nifeather.morph.storage.skill.SkillsConfigurationStoreNew;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataStoreNew extends DirectoryJsonBasedStorage<PlayerMeta> implements IManagePlayerData
{
    private static final PlayerMeta defaultMeta = new PlayerMeta();

    public PlayerDataStoreNew()
    {
        super("playerdata");
    }

    @Initializer
    private void load()
    {
        var packageVersion = this.getPackageVersion();

        if (packageVersion < TARGET_PACKAGE_VERSION)
            update(packageVersion);
    }

    private static final int TARGET_PACKAGE_VERSION = SkillsConfigurationStoreNew.PackageVersions.INITIAL;

    private void update(int currentVersion)
    {
        if (currentVersion < PackageVersions.INITIAL)
        {
            var legacyDataFile = new File(this.plugin.getDataFolder(), "data.json");

            if (legacyDataFile.exists())
                migrateFromLegacyStorage();
        }

        setPackageVersion(TARGET_PACKAGE_VERSION);
    }

    @SuppressWarnings("removal")
    private void migrateFromLegacyStorage()
    {
        logger.info("Migrating player data...");

        var legacyStorage = new LegacyPlayerDataStore();
        legacyStorage.initializeStorage();

        legacyStorage.getAll().forEach(this::save);

        var file = legacyStorage.file();
        var success = file.renameTo(new File(file.getParent(), "data.old.json"));

        if (!success)
            logger.info("Can't rename 'data.json' to 'data.old.json', but it's not a big deal, I guess...");


        logger.info("Done migrating player data!");
    }

    public void save(PlayerMeta playerMeta)
    {
        var uuid = playerMeta.uniqueId;

        if (uuid == null)
        {
            logger.warn("Found a PlayerMeta that doesn't have an UUID! Ignoring...");
            return;
        }

        var path = this.getPath(playerMeta.uniqueId.toString()) + ".json";

        var file = this.directoryStorage.getFile(path, true);
        if (file == null)
        {
            logger.warn("Cannot save disguise configuration for " + uuid);
            return;
        }

        String json = gson.toJson(playerMeta);
        try
        {
            FileUtils.writeStringToFile(file, json, StandardCharsets.UTF_8);
        }
        catch (Throwable t)
        {
            logger.error("Can't write content to file: " + t.getMessage());
        }
    }

    @Override
    protected PlayerMeta getDefault()
    {
        return defaultMeta;
    }

    //region IManagePlayerData

    private final Map<String, DisguiseMeta> cachedMetas = new ConcurrentHashMap<>();

    /**
     * 获取包含某一玩家的玩家名的伪装信息
     *
     * @param rawString 原始ID
     * @return 伪装信息
     * @apiNote 如果原始ID不是有效ID，则会返回null
     */
    @Override
    public @Nullable DisguiseMeta getDisguiseMeta(String rawString)
    {
        var cached = cachedMetas.getOrDefault(rawString, null);
        if (cached != null) return cached;

        var type = DisguiseTypes.fromId(rawString);

        var meta = new DisguiseMeta(rawString, type);
        cachedMetas.put(rawString, meta);

        return meta;
    }

    /**
     * 获取某一玩家所有可用的伪装
     *
     * @param player 目标玩家
     * @return 目标玩家拥有的伪装
     */
    @Override
    public ObjectArrayList<DisguiseMeta> getAvaliableDisguisesFor(Player player)
    {
        return getPlayerMeta(player).getUnlockedDisguises();
    }

    /**
     * 将伪装授予某一玩家
     *
     * @param player             要授予的玩家
     * @param disguiseIdentifier 伪装ID
     * @return 添加是否成功（伪装是否可用或玩家是否已经拥有目标伪装）
     */
    @Override
    public boolean grantMorphToPlayer(Player player, String disguiseIdentifier)
    {
        var playerMeta = this.getPlayerMeta(player);
        var disguiseMeta = this.getDisguiseMeta(disguiseIdentifier);

        if (disguiseMeta == null) return false;

        if (playerMeta.getUnlockedDisguiseIdentifiers()
                .stream()
                .anyMatch(str -> str.equalsIgnoreCase(disguiseIdentifier)))
        {
            return false;
        }

        playerMeta.addDisguise(disguiseMeta);
        save(playerMeta);

        return true;
    }

    /**
     * 从某一玩家剥离伪装
     *
     * @param player             要授予的玩家
     * @param disguiseIdentifier 伪装ID
     * @return 添加是否成功（伪装是否可用或玩家是否已经拥有目标伪装）
     */
    @Override
    public boolean revokeMorphFromPlayer(Player player, String disguiseIdentifier)
    {
        var playerMeta = getPlayerMeta(player);
        var match = playerMeta.getUnlockedDisguises()
                .stream()
                .filter(meta -> meta.equals(disguiseIdentifier))
                .findFirst()
                .orElse(null);

        if (match == null) return false;

        playerMeta.removeDisguise(match);

        return true;
    }

    private final Map<UUID, PlayerMeta> trackedPlayerMetaMap = new ConcurrentHashMap<>();

    /**
     * 获取玩家的伪装配置
     *
     * @param player 目标玩家
     * @return 伪装信息
     */
    @Override
    public PlayerMeta getPlayerMeta(OfflinePlayer player)
    {
        var uuid = player.getUniqueId();

        var tracked = trackedPlayerMetaMap.getOrDefault(uuid, null);
        if (tracked != null) return tracked;

        var storedMeta = this.get(uuid.toString());
        if (storedMeta != null)
        {
            trackedPlayerMetaMap.put(uuid, storedMeta);
            return storedMeta;
        }

        var metaInstance = new PlayerMeta();
        metaInstance.uniqueId = player.getUniqueId();
        metaInstance.playerName = player.getName();

        trackedPlayerMetaMap.put(uuid, metaInstance);

        return metaInstance;
    }

    @Override
    public boolean reloadConfiguration()
    {
        trackedPlayerMetaMap.clear();
        return true;
    }

    @Override
    public boolean saveConfiguration()
    {
        this.trackedPlayerMetaMap.forEach((uuid, meta) -> this.save(meta));

        return true;
    }

    //endregion IManagePlayerData

    public List<PlayerMeta> getAll()
    {
        loadAll();

        return this.trackedPlayerMetaMap.values().stream().toList();
    }

    public void loadAll()
    {
        var files = this.directoryStorage.getFiles();
        for (File file : files)
        {
            if (file.isDirectory()) return;

            var fileName = file.getName();
            UUID uuid = null;

            try
            {
                uuid = UUID.fromString(fileName);
            }
            catch (Throwable ignored)
            {
            }

            if (uuid == null || this.trackedPlayerMetaMap.containsKey(uuid)) return;

            var meta = this.get(fileName);
            if (meta == null) return;

            this.trackedPlayerMetaMap.put(uuid, meta);
        }
    }

    public static class PackageVersions
    {
        public static final int INITIAL = 1;
    }
}
