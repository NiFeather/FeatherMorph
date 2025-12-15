package xyz.nifeather.morph.storage.playerdata;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.storage.IPlayerDataBackend;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.DisguiseTypes;
import xyz.nifeather.morph.storage.DirectoryJsonBasedStorage;
import xyz.nifeather.morph.storage.playerdata.legacy.LegacyPlayerDataStore;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerDataStoreNew extends DirectoryJsonBasedStorage<PlayerMeta> implements IPlayerDataBackend
{
    private static final PlayerMeta defaultMeta = new PlayerMeta();

    public PlayerDataStoreNew()
    {
        super("playerdata");

        var packageVersion = this.getPackageVersion();

        if (packageVersion < TARGET_PACKAGE_VERSION)
            update(packageVersion);

        defaultMeta.uniqueId = UUID.fromString("0-0-0-0-0");
        defaultMeta.playerName = "~RESERVED META, SHOULD NOT BE USED~";
    }

    private static final int TARGET_PACKAGE_VERSION = PackageVersions.INITIAL;

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
        var success = file.renameTo(new File(file.getParent(), "data.json.old"));

        if (!success)
            logger.info("Can't rename 'data.json' to 'data.json.old', but it's not a big deal, I guess...");


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
    public @NotNull DisguiseMeta getDisguiseMeta(String rawString)
    {
        var cached = cachedMetas.getOrDefault(rawString, null);
        if (cached != null) return cached;

        var type = DisguiseTypes.fromId(rawString);

        var meta = new DisguiseMeta(rawString, type);
        cachedMetas.put(rawString, meta);

        return meta;
    }

    @Override
    public void clearCache()
    {
        super.clearCache();
        cachedMetas.clear();
    }

    private @NotNull PlayerMeta loadAndCache(UUID uuid)
    {
        var storedMeta = this.get(uuid.toString());

        // Don't process default meta
        if (!isDefaultMeta(storedMeta))
        {
            storedMeta.playerName = "Not available";
            initializePlayerMeta(storedMeta, uuid);

            trackedPlayerMetaMap.put(uuid, storedMeta);

            return storedMeta;
        }

        var metaInstance = new PlayerMeta();
        metaInstance.uniqueId = uuid;
        metaInstance.playerName = "Not available";
        initializePlayerMeta(metaInstance, uuid);

        trackedPlayerMetaMap.put(uuid, metaInstance);

        return metaInstance;
    }

    private final Map<UUID, CompletableFuture<PlayerMeta>> loadTaskMap = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<PlayerMeta> loadAsync(UUID uuid)
    {
        var existing = loadTaskMap.getOrDefault(uuid, null);
        if (existing != null)
        {
            logger.info("Already have a existing loading task! not creating new task");
            return existing;
        }

        var future = CompletableFuture.supplyAsync(() -> loadAndCache(uuid));

        loadTaskMap.put(uuid, future);

        future.thenAccept(meta ->
        {
            var result = loadTaskMap.remove(uuid, future);
            logger.info("Load complete! remove with result" + result);
        });

        return future;
    }

    /**
     * Get or load data for the given UUID
     *
     * @param uuid
     */
    @Override
    public CompletableFuture<PlayerMeta> getOrLoad(UUID uuid)
    {
        var existing = getIfLoaded(uuid);
        if (existing != null) return CompletableFuture.completedFuture(existing);

        return loadAsync(uuid);
    }

    /**
     * Gets the target UUID's player meta, {@code null} if not loaded
     *
     * @param uuid
     */
    @Override
    public @Nullable PlayerMeta getIfLoaded(UUID uuid)
    {
        return this.trackedPlayerMetaMap.getOrDefault(uuid, null);
    }

    /**
     * WIP experimental
     *
     * @param uuid
     * @param disguiseIdentifier
     * @return
     */
    @Override
    public CompletableFuture<Boolean> grantMorphToPlayerAsync(UUID uuid, String disguiseIdentifier)
    {
        return getOrLoad(uuid).thenApply(playerMeta ->
        {
            var disguiseMeta = this.getDisguiseMeta(disguiseIdentifier);

            if (playerMeta.getUnlockedDisguiseIdentifiers()
                    .stream()
                    .anyMatch(str -> str.equalsIgnoreCase(disguiseIdentifier)))
            {
                return false;
            }

            playerMeta.addDisguise(disguiseMeta);
            save(playerMeta);

            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> revokeMorphFromPlayerAsync(UUID uuid, String disguiseIdentifier)
    {
        return getOrLoad(uuid).thenApply(playerMeta ->
        {
            var match = playerMeta.getUnlockedDisguises()
                    .stream()
                    .filter(meta -> meta.equals(disguiseIdentifier))
                    .findFirst()
                    .orElse(null);

            if (match == null) return false;

            playerMeta.removeDisguise(match);

            return true;
        });
    }

    private final Map<UUID, PlayerMeta> trackedPlayerMetaMap = new ConcurrentHashMap<>();

    private boolean isDefaultMeta(@Nullable PlayerMeta meta)
    {
        return meta == null || meta.equals(defaultMeta);
    }

    private void initializePlayerMeta(PlayerMeta meta, UUID matchingUUID)
    {
        meta.uniqueId = matchingUUID;

        if (FeatherMorphMain.getInstance().debugOutputEnabled())
            logger.info("Doing init for " + meta);

        //要设置给c.unlockedDisguises的列表
        var list = new ObjectArrayList<DisguiseMeta>();

        //原始列表
        var unlockedDisguiseIdentifiers = new ObjectArrayList<>(meta.getUnlockedDisguiseIdentifiers());

        //先对原始列表排序
        unlockedDisguiseIdentifiers.sort(null);

        //然后逐个添加
        unlockedDisguiseIdentifiers.forEach(disguiseId ->
        {
            var type = DisguiseTypes.fromId(disguiseId);

            if (type != null)
                list.add(new DisguiseMeta(disguiseId, DisguiseTypes.fromId(disguiseId)));
            else
                logger.warn("Unknown disguise identifier data '%s' owned by '%s'".formatted(disguiseId, matchingUUID));
        });

        //设置可用的伪装列表并对其加锁
        meta.setUnlockedDisguises(list);
    }

    @Override
    public boolean reload()
    {
        clearCache();
        trackedPlayerMetaMap.clear();

        if (noLazyLoad.get())
            loadAll();

        return true;
    }

    @Override
    public boolean save()
    {
        this.trackedPlayerMetaMap.forEach((uuid, meta) -> this.save(meta));

        return true;
    }

    //endregion IManagePlayerData

    private final AtomicBoolean noLazyLoad = new AtomicBoolean(false);

    public void loadAll()
    {
        logger.info("Force loading all player data...");
        var files = this.directoryStorage.getFiles();

        int count = 0;
        for (File file : files)
        {
            if (file.isDirectory()) continue;

            var fileName = file.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf("."));

            UUID uuid = null;

            try
            {
                uuid = UUID.fromString(fileName);
            }
            catch (Throwable ignored)
            {
            }

            if (uuid == null || this.trackedPlayerMetaMap.containsKey(uuid)) continue;

            var meta = this.get(fileName);
            if (isDefaultMeta(meta)) continue;

            initializePlayerMeta(meta, uuid);

            this.trackedPlayerMetaMap.put(uuid, meta);
            count++;
        }

        logger.info("Loaded %s player data".formatted(count));
    }

    public static class PackageVersions
    {
        public static final int INITIAL = 1;
    }
}
