package xiamomc.morph.misc.skins;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.MorphGameProfile;
import xiamomc.morph.misc.NmsRecord;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerSkinProvider extends MorphPluginObject
{
    private static PlayerSkinProvider instance;

    public static PlayerSkinProvider getInstance()
    {
        if (instance == null)
        {
            instance = new PlayerSkinProvider();
            instance.skinCache.initializeStorage();
        }

        return instance;
    }

    private CompletableFuture<Optional<GameProfile>> getProfileAsyncV2(String name)
    {
        var executor = Util.PROFILE_EXECUTOR;

        return CompletableFuture.supplyAsync(() -> this.fetchProfileV2(name), executor)
                .whenCompleteAsync((optional, throwable) -> {}, executor);
    }

    private final SkinCache skinCache = new SkinCache();

    /**
     * 根据给定的名称搜索对应的Profile（不包含皮肤）
     * @apiNote 此方法返回的GameProfile不包含皮肤，若要获取于此对应的皮肤，请使用 {@link PlayerSkinProvider#fetchSkinFromProfile(GameProfile)}
     * @param name
     * @return
     */
    private Optional<GameProfile> fetchProfileV2(String name)
    {
        if (!Player.isValidUsername(name))
            return Optional.empty();

        var profileRef = new AtomicReference<GameProfile>(null);

        var lookupCallback = new ProfileLookupCallback()
        {
            public void onProfileLookupSucceeded(GameProfile gameprofile)
            {
                profileRef.set(gameprofile);
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
            }
        };

        GameProfileRepository profileRepo = MinecraftServer.getServer().getProfileRepository();
        profileRepo.findProfilesByNames(new String[]{name}, lookupCallback);

        var profile = profileRef.get();
        return profile == null ? Optional.empty() : Optional.of(profile);
    }

    /**
     * 通过给定的Profile获取与其对应的皮肤
     * @param profile 目标GameProfile
     */
    public CompletableFuture<Optional<GameProfile>> fetchSkinFromProfile(GameProfile profile)
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

                return result == null ? Optional.of(profile) : Optional.of(result.profile());
            });
        }
    }

    @Nullable
    public GameProfile getCachedProfile(String name)
    {
        return skinCache.get(name).profileOptional().orElse(null);
    }

    public void cacheProfile(PlayerProfile playerProfile)
    {
        var gameProfile = new MorphGameProfile(playerProfile);
        skinCache.cache(gameProfile);
    }

    private final Map<String, CompletableFuture<Optional<GameProfile>>> onGoingRequests = new ConcurrentHashMap<>();

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

        var req = getProfileAsyncV2(profileName)
                .thenCompose(rawProfileOptional ->
                {
                    if (rawProfileOptional.isPresent()) //如果查有此人，那么继续流程
                    {
                        return fetchSkinFromProfile(rawProfileOptional.get());
                    }
                    else if (cachedSkin.profileOptional().isPresent()) //否则，如果本地有缓存，那就使用本地缓存
                    {
                        //重新缓存让此皮肤脱离过期状态
                        //因为已经查无此人了，没有必要再短时间内重新查询此人的皮肤
                        skinCache.cache(cachedSkin.profileOptional().get());
                        return CompletableFuture.completedFuture(cachedSkin.profileOptional());
                    }
                    else
                    {
                        return CompletableFuture.completedFuture(Optional.empty());
                    }
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
