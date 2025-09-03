package xyz.nifeather.morph.api.direct;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Managers.DependencyManager;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.abilities.AbilityManager;
import xyz.nifeather.morph.interfaces.IManageRequests;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.skills.SkillManager;

public class FeatherMorphDirectAccess
{
    private final FeatherMorphMain plugin;

    private final DependencyManager dependencyManager;

    public FeatherMorphDirectAccess(FeatherMorphMain pl)
    {
        this.plugin = pl;

        this.dependencyManager = DependencyManager.getInstance(pl.getNamespace());

        this.morphManager = dependencyManager.get(MorphManager.class, true);
        this.skillHandler = dependencyManager.get(SkillManager.class, true);
        this.abilityManager = dependencyManager.get(AbilityManager.class, true);
        this.requestManager = dependencyManager.get(IManageRequests.class, true);
        this.clientHandler = dependencyManager.get(MorphClientHandler.class, true);
        this.revealingHandler = dependencyManager.get(RevealingHandler.class, true);
    }

    public FeatherMorphMain plugin()
    {
        return plugin;
    }

    private final MorphManager morphManager;
    private final SkillManager skillHandler;
    private final AbilityManager abilityManager;
    private final IManageRequests requestManager;
    private final MorphClientHandler clientHandler;
    private final RevealingHandler revealingHandler;

    /**
     * The MorphManager, mainly handling these functions:
     * <br>
     * 1. Disguise sessions (Morph/Unmorph, {@link xyz.nifeather.morph.misc.DisguiseState}s)
     * <br>
     * 2. Player data (Grant/Revoke disguises)
     * <br>
     * 3. Backend and Disguise Providers
     */
    public MorphManager morphManager()
    {
        return morphManager;
    }

    public RevealingHandler revealingHandler()
    {
        return revealingHandler;
    }

    public SkillManager skillHandler()
    {
        return skillHandler;
    }

    public AbilityManager abilityManager()
    {
        return abilityManager;
    }

    public IManageRequests requestManager()
    {
        return requestManager;
    }

    public MorphClientHandler clientHandler()
    {
        return clientHandler;
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
    public <T> T getGlobalDependency(Class<T> clazz, boolean throwIfNotFound)
    {
        return dependencyManager.get(clazz, throwIfNotFound);
    }
}
