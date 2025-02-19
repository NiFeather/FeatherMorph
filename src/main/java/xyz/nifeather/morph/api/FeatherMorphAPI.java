package xyz.nifeather.morph.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.api.direct.FeatherMorphDirectAccess;
import xyz.nifeather.morph.api.utilties.v0.UtilitiesAlpha;

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
