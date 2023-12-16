package xiamomc.morph.misc.skins;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PlayerSkinProvider extends MorphPluginObject
{
    private static PlayerSkinProvider instance;

    public static PlayerSkinProvider getInstance()
    {
        if (instance == null) instance = new PlayerSkinProvider();

        return instance;
    }

    private GameProfileCache userCache;

    @Resolved
    private SkinStore skinStore;

    @Initializer
    private void load()
    {
        var server = MinecraftServer.getServer();
        //userCache = new GameProfileCache(server.getProfileRepository(), new File("/dev/null"));
        //userCache.setExecutor(server);
    }

    public CompletableFuture<Optional<GameProfile>> fetchSkinFromProfile(GameProfile profile)
    {
        if (profile.getProperties().containsKey("textures"))
        {
            var optional = Optional.of(profile);
            profileCache.put(profile.getName(), ProfileMeta.of(profile));

            return CompletableFuture.completedFuture(optional);
        }
        else
        {
            return CompletableFuture.supplyAsync(() ->
            {
                var sessionService = MinecraftServer.getServer().getSessionService();
                var result = sessionService.fetchProfile(profile.getId(), true);
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

    private final Map<String, ProfileMeta> profileCache = new Object2ObjectOpenHashMap<>();

    @Nullable
    public GameProfile getCachedProfile(String profileName)
    {
        var meta = profileCache.getOrDefault(profileName, null);
        if (meta != null)
        {
            meta.lastAccess = plugin.getCurrentTick();
            return meta.profile;
        }

        return null;
    }

    private void removeUnusedMeta()
    {
        var currentTick = plugin.getCurrentTick();
        var mapCopy = new Object2ObjectOpenHashMap<>(profileCache);
        mapCopy.forEach((name, meta) ->
        {
            if (currentTick - meta.lastAccess > 20 * 60 * 30)
                profileCache.remove(name);
        });
    }

    private int performCount;

    public CompletableFuture<Optional<GameProfile>> fetchSkin(String profileName)
    {
        performCount++;

        if (performCount >= 10)
            removeUnusedMeta();

        var player = Bukkit.getPlayerExact(profileName);
        if (player != null)
        {
            var profile = NmsRecord.ofPlayer(player).gameProfile;
            profileCache.put(profileName, ProfileMeta.of(profile));
            return CompletableFuture.completedFuture(Optional.of(profile));
        }

        var cachedSkin = getCachedProfile(profileName);
        if (cachedSkin != null)
            return CompletableFuture.completedFuture(Optional.of(cachedSkin));

        var server = MinecraftServer.getServer();
        var userCache = this.userCache == null
                        ? server.getProfileCache()
                        : this.userCache; //new GameProfileCache(server.getProfileRepository(), new File("/dev/null"));

        if (userCache == null) return CompletableFuture.completedFuture(Optional.empty());
        else
        {
            return userCache.getAsync(profileName)
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
}
