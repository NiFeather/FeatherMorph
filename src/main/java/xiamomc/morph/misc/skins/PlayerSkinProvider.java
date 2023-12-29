package xiamomc.morph.misc.skins;

import com.destroystokyo.paper.profile.PaperAuthenticationService;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.pluginbase.Annotations.Initializer;

import java.io.File;
import java.net.Proxy;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerSkinProvider extends MorphPluginObject
{
    private static PlayerSkinProvider instance;

    public static PlayerSkinProvider getInstance()
    {
        if (instance == null) instance = new PlayerSkinProvider();

        return instance;
    }

    private CompletableFuture<Optional<GameProfile>> getProfileAsyncV2(String name)
    {
        var executor = Util.PROFILE_EXECUTOR;

        var task = CompletableFuture.supplyAsync(() ->
        {
            return this.fetchProfileV2(name);
        }, executor).whenCompleteAsync((optional, throwable) ->
        {
        }, executor);

        return task;
    }

    private final SkinStore skinStore = new SkinStore();

    private Optional<GameProfile> fetchProfileV2(String name)
    {
        if (!Player.isValidUsername(name))
            return Optional.empty();

        var cached = skinStore.get(name);
        if (cached != null)
            return Optional.of(cached);

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
                    exception.printStackTrace();
                }
            }
        };

        GameProfileRepository gameProfileRepository = MinecraftServer.getServer().getProfileRepository();
        gameProfileRepository.findProfilesByNames(new String[]{name}, lookupCallback);

        var profile = profileRef.get();
        return profile == null ? Optional.empty() : Optional.of(profile);
    }

    @Nullable
    public GameProfile getCachedProfile(String name)
    {
        return skinStore.get(name);
    }

    public CompletableFuture<Optional<GameProfile>> fetchSkinFromProfile(GameProfile profile)
    {
        if (profile.getProperties().containsKey("textures"))
        {
            var optional = Optional.of(profile);
            skinStore.cache(profile);

            return CompletableFuture.completedFuture(optional);
        }
        else
        {
            return CompletableFuture.supplyAsync(() ->
            {
                var sessionService = MinecraftServer.getServer().getSessionService();
                var result = sessionService.fetchProfile(profile.getId(), true);

                if (result != null)
                    skinStore.cache(result.profile());

                return result == null ? Optional.of(profile) : Optional.of(result.profile());
            });
        }
    }

    private static class ProfileMeta
    {
        private final GameProfile profile;
        private long lastAccess;

        public ProfileMeta(GameProfile profile, long creationTime)
        {
            this.profile = profile;
            lastAccess = creationTime;
        }

        public static ProfileMeta of(GameProfile profile)
        {
            return new ProfileMeta(
                    profile, MorphPlugin.getInstance().getCurrentTick()
            );
        }
    }

    private int performCount;

    public CompletableFuture<Optional<GameProfile>> fetchSkin(String profileName)
    {
        performCount++;

        var player = Bukkit.getPlayerExact(profileName);
        if (player != null)
        {
            var profile = NmsRecord.ofPlayer(player).gameProfile;
            skinStore.remove(profileName);
            return CompletableFuture.completedFuture(Optional.of(profile));
        }

        var cachedSkin = getCachedProfile(profileName);
        if (cachedSkin != null)
            return CompletableFuture.completedFuture(Optional.of(cachedSkin));

        return getProfileAsyncV2(profileName)
                .thenCompose(rawProfileOptional ->
                {
                    if (rawProfileOptional.isPresent())
                    {
                        return fetchSkinFromProfile(rawProfileOptional.get());
                    }
                    else
                    {
                        return CompletableFuture.completedFuture(Optional.empty());
                    }
                });
    }
}
