package xyz.nifeather.morph.api;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.api.direct.FeatherMorphDirectAccess;
import xyz.nifeather.morph.api.utilties.v0.UtilitiesAlpha;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@ApiStatus.Experimental
public class FeatherMorphAPI
{
    //region static stuffs

    /**
     * @return NULL if the API haven't initialized yet.
     */
    @Nullable
    public static FeatherMorphAPI instance()
    {
        return instance;
    }

    @Nullable
    private static FeatherMorphAPI instance;

    /**
     * @deprecated Use {@link FeatherMorphAPI#getApiFuture()} instead.
     */
    @Deprecated(forRemoval = true)
    public static void runWhenAPILoaded(Runnable runnable)
    {
        getApiFuture().thenAccept(api -> runnable.run());
    }

    //region CompletableFuture

    private static final CompletableFuture<FeatherMorphAPI> apiFuture = new CompletableFuture<>();

    /**
     * Gets a CompletableFuture which is called after the plugin initialized
     */
    public static CompletableFuture<FeatherMorphAPI> getApiFuture()
    {
        var listeningFuture = new CompletableFuture<FeatherMorphAPI>();

        apiFuture.exceptionally(e ->
        {
            listeningFuture.completeExceptionally(e);
            return null;
        });

        apiFuture.thenAccept(listeningFuture::complete);

        return listeningFuture;
    }

    private static final CompletableFuture<Object> panicFuture = new CompletableFuture<>();

    @ApiStatus.Internal
    public static void panic()
    {
        panicFuture.completeExceptionally(new Exception("Plugin Panic"));
    }

    /**
     * Gets a CompletableFuture which is called after the {@link FeatherMorphMain#panic(String...)} is called
     * @apiNote This future will never get completed,
     */
    public static void listenForPluginPanic(Runnable runnable)
    {
        panicFuture.exceptionally(e ->
        {
            runnable.run();
            return null;
        });
    }

    //endregion CompletableFuture

    //endregion static stuffs

    @NotNull
    private final FeatherMorphMain plugin;

    private final FeatherMorphDirectAccess directAccess;
    private final UtilitiesAlpha utilsAlpha;
    private final APIMeta apiMeta;

    public FeatherMorphAPI(FeatherMorphMain plugin)
    {
        var logger = plugin.getSLF4JLogger();
        logger.info("Running init for FeatherMorphAPI...");

        this.plugin = plugin;
        directAccess = new FeatherMorphDirectAccess(plugin);

        utilsAlpha = new UtilitiesAlpha(directAccess);

        apiMeta = new APIMeta();

        instance = this;
        apiFuture.complete(this);

        logger.info("Done running init for FeatherMorphAPI");
    }

    /**
     * Gets the metadata of the API impl.
     * @apiNote You can also use {@link APIMeta} directly.
     */
    public APIMeta getMeta()
    {
        return apiMeta;
    }

    /**
     * Returns the direct access to several components of FeatherMorph.
     */
    public FeatherMorphDirectAccess directAccess()
    {
        return directAccess;
    }

    /**
     * Some utilities, still under development.
     */
    public UtilitiesAlpha utilitiesAlpha()
    {
        return utilsAlpha;
    }
}
