package xyz.nifeather.morph.misc.skins;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.misc.MorphGameProfile;
import xyz.nifeather.morph.misc.NmsRecord;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class PlayerSkinProvider extends MorphPluginObject
{
    public static PlayerSkinProvider getInstance()
    {
        return PlayerSkinProviderLazyHolder.instance;
    }

    private static class PlayerSkinProviderLazyHolder
    {
        public static final PlayerSkinProvider instance = new PlayerSkinProvider();
    }

    public PlayerSkinProvider()
    {
        this.skinCache.initializeStorage();
    }

    @Initializer
    private void load()
    {
        Bukkit.getAsyncScheduler().runAtFixedRate(plugin,
                task -> this.batchPlayerInfo(),
                1500, 1500,
                TimeUnit.MILLISECONDS);
    }

    private final SkinCache skinCache = new SkinCache();

    // region Info Request Batching

    private final Map<String, CompletableFuture<Optional<GameProfile>>> namesToLookup = new ConcurrentHashMap<>();

    private CompletableFuture<Optional<GameProfile>> fetchPlayerInfoAsync(String name)
    {
        var future = new CompletableFuture<Optional<GameProfile>>();

        synchronized (batchLock)
        {
            this.namesToLookup.put(name, future);
        }

        return future;
    }

    private static final Object batchLock = new Object();

    /**
     * 批量处理玩家信息请求
     */
    public void batchPlayerInfo()
    {
        synchronized (batchLock)
        {
            startBatchPlayerInfo();
        }
    }

    private void startBatchPlayerInfo()
    {
        if (namesToLookup.isEmpty()) return;

        Map<String, CompletableFuture<Optional<GameProfile>>> toBatch = new Object2ObjectOpenHashMap<>();

        synchronized (namesToLookup)
        {
            int currentPickIndex = 0;
            var targetAmount = Math.min(this.namesToLookup.size(), 10);

            for (String name : new ObjectArrayList<>(this.namesToLookup.keySet()))
            {
                if (currentPickIndex >= targetAmount) break;

                var future = this.namesToLookup.remove(name);
                toBatch.put(name, future);
                currentPickIndex++;
            }
        }

        List<String> remainingNames = new ObjectArrayList<>(toBatch.keySet());

        BiConsumer<String, @Nullable GameProfile> onRequestFinish = (name, profile) ->
        {
            Optional<GameProfile> optional = profile == null ? Optional.empty() : Optional.of(profile);

            remainingNames.remove(name);

            var future = toBatch.getOrDefault(name, null);
            if (future == null)
                logger.warn("Profile with name '%s' configured a lookup request but no callback is set?!".formatted(name));
            else
                future.complete(optional);
        };

        var lookupCallback = new ProfileLookupCallback()
        {
            public void onProfileLookupSucceeded(GameProfile gameprofile)
            {
                onRequestFinish.accept(gameprofile.getName(), gameprofile);
            }

            public void onProfileLookupFailed(String profileName, Exception exception)
            {
                if (exception instanceof ProfileNotFoundException)
                {
                    //do nothing
                }
                else if (exception instanceof AuthenticationUnavailableException)
                {
                    logger.info("Failed to lookup '%s' because the authentication service is not available now...".formatted(profileName));
                }
                else
                {
                    logger.info("Failed to lookup '%s': '%s'".formatted(profileName, exception.getMessage()));
                }

                onRequestFinish.accept(profileName, null);
            }
        };

        GameProfileRepository profileRepo = MinecraftServer.getServer().getProfileRepository();
        profileRepo.findProfilesByNames(remainingNames.toArray(new String[0]), lookupCallback);
    }

    // endregion Info Request Batching

    @Nullable
    public GameProfile getCachedProfile(String name)
    {
        return skinCache.get(name).profileOptional().orElse(null);
    }

    public void cacheProfile(@NotNull PlayerProfile playerProfile)
    {
        var gameProfile = new MorphGameProfile(playerProfile);
        skinCache.cache(gameProfile);
    }

    private final Map<String, CompletableFuture<Optional<GameProfile>>> onGoingRequests = new ConcurrentHashMap<>();

    /**
     * 通过给定的Profile获取与其对应的皮肤
     * @param profile 目标GameProfile
     */
    public CompletableFuture<Optional<GameProfile>> fetchSkin(GameProfile profile)
    {
        if (profile.getProperties().containsKey("textures"))
        {
            var optional = Optional.of(profile);
            skinCache.cache(profile);

            return CompletableFuture.completedFuture(optional);
        }
        else
        {
            return CompletableFuture.supplyAsync(() ->
            {
                var sessionService = MinecraftServer.getServer().getSessionService();
                var result = sessionService.fetchProfile(profile.getId(), true);

                if (result != null)
                    skinCache.cache(result.profile());
                else
                    skinCache.cache(new GameProfile(Util.NIL_UUID, profile.getName()));

                return result == null ? Optional.of(profile) : Optional.of(result.profile());
            });
        }
    }

    /**
     * 尝试获取与给定名称对应的皮肤
     * @param profileName 目标名称
     * @return
     */
    public CompletableFuture<Optional<GameProfile>> fetchSkin(String profileName)
    {
        var player = Bukkit.getPlayerExact(profileName);
        if (player != null && player.getPlayerProfile().hasTextures())
        {
            var profile = NmsRecord.ofPlayer(player).gameProfile;
            skinCache.cache(profile);

            return CompletableFuture.completedFuture(Optional.of(profile));
        }

        var cachedSkin = skinCache.get(profileName);

        //如果Record的皮肤没有过期并且有值，那么使用此Record
        //否则仍然进行获取流程
        if (!cachedSkin.expired() && cachedSkin.profileOptional().isPresent())
            return CompletableFuture.completedFuture(cachedSkin.profileOptional());

        //如果此profile有正在进行的请求，那么直接复用
        var prevReq = onGoingRequests.getOrDefault(profileName, null);
        if (prevReq != null)
            return prevReq;

        var req = fetchPlayerInfoAsync(profileName)
                .thenCompose(rawProfileOptional ->
                {
                    if (rawProfileOptional.isPresent()) //如果查有此人，那么继续流程
                    {
                        return fetchSkin(rawProfileOptional.get());
                    }
                    else if (cachedSkin.profileOptional().isPresent()) //否则，如果本地有缓存，那就使用本地缓存
                    {
                        //重新缓存让此皮肤脱离过期状态
                        //因为已经查无此人了，没有必要再短时间内重新查询此人的皮肤
                        skinCache.cache(cachedSkin.profileOptional().get());
                        return CompletableFuture.completedFuture(cachedSkin.profileOptional());
                    }
                    else //本地没有缓存，则创建一个空Profile
                    {
                        skinCache.cache(new GameProfile(Util.NIL_UUID, profileName));
                        return CompletableFuture.completedFuture(Optional.empty());
                    }
                });

        req.exceptionally(t ->
        {
            onGoingRequests.remove(profileName);
            return Optional.empty();
        });
        req.thenRun(() -> onGoingRequests.remove(profileName));

        onGoingRequests.put(profileName, req);

        return req;
    }

    //region Utils

    public List<SingleSkin> getAllSkins()
    {
        return skinCache.listAll();
    }

    public void dropSkin(String name)
    {
        skinCache.drop(name);
    }

    public void dropAll()
    {
        skinCache.dropAll();
    }

    public void reload()
    {
        skinCache.reloadConfiguration();
    }

    public void invalidate(String name)
    {
        var skin = skinCache.getRaw(name);
        if(skin == null) return;

        skin.expiresAt = 0;
    }

    //endregion
}
