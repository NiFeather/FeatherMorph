package xyz.nifeather.morph.api.direct;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Managers.DependencyManager;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RequestManager;
import xyz.nifeather.morph.abilities.AbilityManager;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.skills.MorphSkillHandler;

public class FeatherMorphDirectAccess
{
    private final FeatherMorphMain plugin;

    private final DependencyManager dependencyManager;

    public FeatherMorphDirectAccess(FeatherMorphMain pl)
    {
        this.plugin = pl;

        this.dependencyManager = DependencyManager.getInstance(pl.getNamespace());
    }

    /**
     * The MorphManager, handles these functions:
     * <br>
     * 1. Disguise sessions ({@link xyz.nifeather.morph.misc.DisguiseState}s)
     * <br>
     * 2. Player data (Grant/Revoke disguises)
     * <br>
     * 3. Backend stuffs
     */
    public MorphManager morphManager()
    {
        return dependencyManager.get(MorphManager.class, true);
    }

    public MorphSkillHandler skillHandler()
    {
        return dependencyManager.get(MorphSkillHandler.class, true);
    }

    public AbilityManager abilityManager()
    {
        return dependencyManager.get(AbilityManager.class, true);
    }

    public RequestManager requestManager()
    {
        return dependencyManager.get(RequestManager.class, true);
    }

    public MorphClientHandler clientHandler()
    {
        return dependencyManager.get(MorphClientHandler.class, true);
    }

    /**
     * Lookup for a component in the global dependencies.
     * @param clazz The class of the dependency
     * @throws xiamomc.pluginbase.Exceptions.NullDependencyException if the dependency is not registered
     */
    @NotNull
    public <T> T getGlobalDependency(Class<T> clazz)
    {
        return getGlobalDependency(clazz, true);
    }

    /**
     * Lookup for a component in the global dependencies.
     * @param clazz The class of the dependency
     * @throws xiamomc.pluginbase.Exceptions.NullDependencyException if the dependency is not registered, AND throwIfNotFound is set to true
     */
    @Nullable
    @Contract("_, true -> !null; _, false -> null")
    public <T> T getGlobalDependency(Class<T> clazz, boolean throwIfNotFound)
    {
        return dependencyManager.get(clazz, throwIfNotFound);
    }
}
