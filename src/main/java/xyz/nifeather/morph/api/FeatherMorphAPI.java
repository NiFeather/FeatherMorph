package xyz.nifeather.morph.api;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.api.direct.FeatherMorphDirectAccess;
import xyz.nifeather.morph.api.utilties.v0.UtilitiesAlpha;

import java.util.List;

@ApiStatus.Experimental
public class FeatherMorphAPI
{
    //region static stuffs

    /**
     * @return NULL if the plugin haven't initialized yet.
     */
    @Nullable
    public static FeatherMorphAPI instance()
    {
        return instance;
    }

    @Nullable
    private static FeatherMorphAPI instance;

    private static final List<Runnable> hooks = ObjectLists.synchronize(new ObjectArrayList<>());

    public static void runWhenAPILoaded(Runnable runnable)
    {
        hooks.add(runnable);
    }

    //endregion static stuffs

    @NotNull
    private final FeatherMorphMain plugin;

    private final FeatherMorphDirectAccess directAccess;
    private final UtilitiesAlpha utilsAlpha;

    public FeatherMorphAPI(FeatherMorphMain plugin)
    {
        this.plugin = plugin;
        directAccess = new FeatherMorphDirectAccess(plugin);

        utilsAlpha = new UtilitiesAlpha(directAccess);

        instance = this;
        hooks.forEach(Runnable::run);
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
