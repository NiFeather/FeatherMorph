package xiamomc.morph.backends.server.renderer.skins;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
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
            profileCache.put(profile.getName(), profile);

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

    private final Map<String, GameProfile> profileCache = new Object2ObjectOpenHashMap<>();

    @Nullable
    public GameProfile getCachedProfile(String profileName)
    {
        return profileCache.getOrDefault(profileName, null);
    }

    public CompletableFuture<Optional<GameProfile>> fetchSkin(String profileName)
    {
        var player = Bukkit.getPlayerExact(profileName);
        if (player != null)
        {
            profileCache.remove(profileName);
            return CompletableFuture.completedFuture(Optional.of(NmsRecord.ofPlayer(player).gameProfile));
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
