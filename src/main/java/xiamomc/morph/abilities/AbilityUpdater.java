package xiamomc.morph.abilities;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.permissions.CommonPermissions;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.Arrays;
import java.util.List;

public class AbilityUpdater extends MorphPluginObject
{
    @NotNull
    private final DisguiseState parentState;

    private final List<IMorphAbility<?>> enabledAbilities = new ObjectArrayList<>();
    private final List<IMorphAbility<?>> disabledAbilities = new ObjectArrayList<>();
    private final List<IMorphAbility<?>> pendingAbilities = new ObjectArrayList<>();

    private Bindable<Boolean> checkAbilityPermissions = new Bindable<>(true);

    @Initializer
    private void load(MorphConfigManager config)
    {
        this.checkAbilityPermissions = config.getBindable(Boolean.class, ConfigOption.DO_CHECK_ABILITY_PERMISSIONS);
    }

    public AbilityUpdater(@NotNull DisguiseState parentState)
    {
        this.parentState = parentState;
    }

    private Player player()
    {
        return parentState.getPlayer();
    }

    private void doUpdate()
    {
        List<IMorphAbility<?>> pending = new ObjectArrayList<>();
        synchronized (pendingAbilities)
        {
            pending.addAll(pendingAbilities);
        }

        var player = player();
        if (!pending.isEmpty())
        {
            for (var ability : pending)
            {
                if (hasPermissionFor(ability, parentState))
                {
                    ability.applyToPlayer(player, parentState);
                    enabledAbilities.add(ability);
                }
                else
                {
                    disabledAbilities.add(ability);
                }
            }

            this.pendingAbilities.clear();
        }

        // 如果启用，则每秒只检查4遍权限
        var checkPermissions = checkAbilityPermissions.get() && plugin.getCurrentTick() % 5 == 0;

        for (var ability : enabledAbilities)
        {
            if (!checkPermissions || hasPermissionFor(ability, parentState))
            {
                ability.handle(player, parentState);
            }
            else
            {
                ability.revokeFromPlayer(player, parentState);
                enabledAbilities.remove(ability);
                disabledAbilities.add(ability);
            }
        }
    }

    public synchronized boolean update()
    {
        try
        {
            doUpdate();

            return true;
        }
        catch (Throwable t)
        {
            logger.error("Error occurred updating abilities: " + t.getMessage());
            t.printStackTrace();

            return false;
        }
    }

    public enum OperationResult
    {
        SUCCESS,
        FAIL_UNKNOWN,
        FAIL_ALREADY_EXISTS,
        FAIL_NOT_EXIST
    }

    public synchronized void reApplyAbility()
    {
        enabledAbilities.forEach(a -> a.applyToPlayer(player(), parentState));
    }

    public boolean containsAbility(NamespacedKey identifier)
    {
        return enabledAbilities.stream().anyMatch(a -> a.getIdentifier().equals(identifier))
                || disabledAbilities.stream().anyMatch(a -> a.getIdentifier().equals(identifier));
    }

    /**
     * @return True if all success.
     */
    public synchronized boolean setAbilities(@NotNull List<IMorphAbility<?>> abilities)
    {
        enabledAbilities.forEach(a -> a.revokeFromPlayer(player(), parentState));
        enabledAbilities.clear();
        disabledAbilities.clear();

        return this.addAbilities(abilities);
    }

    public synchronized OperationResult removeAbility(NamespacedKey targetIdentifier)
    {
        IMorphAbility<?> ability = null;

        ability = enabledAbilities.stream()
                .filter(a -> a.getIdentifier().equals(targetIdentifier))
                .findFirst().orElse(null);

        if (ability == null)
        {
            ability = disabledAbilities.stream()
                    .filter(a -> a.getIdentifier().equals(targetIdentifier))
                    .findFirst().orElse(null);
        }

        if (ability == null)
            return OperationResult.FAIL_NOT_EXIST;

        ability.revokeFromPlayer(player(), parentState);
        enabledAbilities.remove(ability);
        disabledAbilities.remove(ability);

        return OperationResult.SUCCESS;
    }

    /**
     * @return True if all success.
     */
    public synchronized boolean addAbilities(IMorphAbility<?>... abilities)
    {
        return addAbilities(Arrays.stream(abilities).toList());
    }

    /**
     * @return True if all success.
     */
    public synchronized boolean addAbilities(List<IMorphAbility<?>> abilities)
    {
        boolean success = true;

        for (IMorphAbility<?> ability : abilities)
            success = success && (addAbility(ability) == OperationResult.SUCCESS);

        return success;
    }

    public synchronized OperationResult addAbility(IMorphAbility<?> ability)
    {
        if (pendingAbilities.stream().anyMatch(a -> a.getIdentifier().equals(ability.getIdentifier())))
            return OperationResult.FAIL_ALREADY_EXISTS;

        pendingAbilities.add(ability);

        return OperationResult.SUCCESS;
    }

    public synchronized List<IMorphAbility<?>> getAbilities()
    {
        var list = new ObjectArrayList<IMorphAbility<?>>();

        list.addAll(enabledAbilities);
        list.addAll(disabledAbilities);

        return list;
    }

    public synchronized void dispose()
    {
        this.enabledAbilities.forEach(a -> a.revokeFromPlayer(player(), parentState));
        this.setAbilities(List.of());
    }

    public static boolean hasPermissionFor(IMorphAbility<?> ability, DisguiseState state)
    {
        var player = state.getPlayer();

        var singleAbilityPerm = CommonPermissions.abilityPermissionOf(ability.getIdentifier().asString(), state.getDisguiseIdentifier());
        return !player.isPermissionSet(singleAbilityPerm) || player.hasPermission(singleAbilityPerm);
    }
}
