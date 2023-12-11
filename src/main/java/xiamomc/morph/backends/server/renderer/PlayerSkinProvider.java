package xiamomc.morph.backends.server.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Annotations.Initializer;

import java.io.File;
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
            return CompletableFuture.completedFuture(Optional.of(profile));
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

    public CompletableFuture<Optional<GameProfile>> fetchSkin(String profileName)
    {
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
