package xiamomc.morph.abilities;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectBooleanMutablePair;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.permissions.CommonPermissions;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AbilityUpdater extends MorphPluginObject
{
    @NotNull
    private final DisguiseState parentState;

    private final List<IMorphAbility<?>> pendingAbilities = new ObjectArrayList<>();

    // <Ability, Enabled?>
    private final List<Pair<IMorphAbility<?>, Boolean>> registeredAbilities = new ObjectArrayList<>();

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
                var hasPermission = !checkAbilityPermissions.get() || hasPermissionFor(ability, parentState);
                var pair = new ObjectBooleanMutablePair<IMorphAbility<?>>(ability, hasPermission);

                if (hasPermission)
                    ability.applyToPlayer(player, parentState);

                registeredAbilities.add(pair);
            }

            this.pendingAbilities.clear();
        }

        for (var abilityPair : registeredAbilities)
        {
            var ability = abilityPair.left();
            var enabled = abilityPair.right();

            if (checkAbilityPermissions.get() && plugin.getCurrentTick() % 5 == 0)
            {
                boolean hasPermission = hasPermissionFor(ability, parentState);

                if (hasPermission && !enabled)
                {
                    enableAbility(abilityPair, player);

                    enabled = true;
                }
                else if (!hasPermission && enabled)
                {
                    disableAbility(abilityPair, player);

                    enabled = false;
                }
            }
            else if (!checkAbilityPermissions.get() && !enabled)
            {
                enableAbility(abilityPair, player);

                enabled = true;
            }

            if (enabled)
                ability.handle(player, parentState);
        }
    }

    private void disableAbility(Pair<IMorphAbility<?>, Boolean> pair, Player player)
    {
        pair.left().revokeFromPlayer(player, parentState);
        pair.right(false);
    }

    private void enableAbility(Pair<IMorphAbility<?>, Boolean> pair, Player player)
    {
        pair.left().applyToPlayer(player, parentState);
        pair.right(true);
    }

    public boolean update()
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

    public void reApplyAbility()
    {
        registeredAbilities.forEach(pair ->
        {
            if (pair.right())
                pair.left().applyToPlayer(player(), parentState);
        });
    }

    public boolean containsAbility(NamespacedKey identifier)
    {
        return registeredAbilities.stream().anyMatch(pair -> pair.left().getIdentifier().equals(identifier));
    }

    @Unmodifiable
    public List<IMorphAbility<?>> getEnabledAbilities()
    {
        return registeredAbilities.stream().filter(Pair::right)
                .map(Pair::left)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * @return True if all success.
     */
    public boolean setAbilities(@NotNull List<IMorphAbility<?>> abilities)
    {
        this.getEnabledAbilities().forEach(a -> a.revokeFromPlayer(player(), parentState));
        this.registeredAbilities.clear();

        return this.addAbilities(abilities);
    }

    @Nullable
    public IMorphAbility<?> getAbilityInstance(NamespacedKey identifier)
    {
        var optional = registeredAbilities.stream().filter(pair -> pair.left().getIdentifier().equals(identifier))
                .findFirst();

        return optional.map(Pair::left).orElse(null);
    }

    public OperationResult removeAbility(NamespacedKey targetIdentifier)
    {
        IMorphAbility<?> ability = getAbilityInstance(targetIdentifier);

        if (ability == null)
            return OperationResult.FAIL_NOT_EXIST;

        ability.revokeFromPlayer(player(), parentState);
        registeredAbilities.removeIf(pair -> pair.left().equals(ability));

        return OperationResult.SUCCESS;
    }

    /**
     * @return True if all success.
     */
    public boolean addAbilities(IMorphAbility<?>... abilities)
    {
        return addAbilities(Arrays.stream(abilities).toList());
    }

    /**
     * @return True if all success.
     */
    public boolean addAbilities(List<IMorphAbility<?>> abilities)
    {
        boolean success = true;

        for (IMorphAbility<?> ability : abilities)
            success = success && (addAbility(ability) == OperationResult.SUCCESS);

        return success;
    }

    public OperationResult addAbility(IMorphAbility<?> ability)
    {
        synchronized (pendingAbilities)
        {
            if (pendingAbilities.stream().anyMatch(a -> a.getIdentifier().equals(ability.getIdentifier())))
                return OperationResult.FAIL_ALREADY_EXISTS;

            pendingAbilities.add(ability);
        }

        return OperationResult.SUCCESS;
    }

    @Unmodifiable
    public List<IMorphAbility<?>> getRegisteredAbilities()
    {
        return this.registeredAbilities.stream().map(Pair::left).collect(Collectors.toUnmodifiableList());
    }

    public void dispose()
    {
        getEnabledAbilities().forEach(a -> a.revokeFromPlayer(player(), parentState));
        this.setAbilities(List.of());
    }

    public static boolean hasPermissionFor(IMorphAbility<?> ability, DisguiseState state)
    {
        var player = state.getPlayer();

        var singleAbilityPerm = CommonPermissions.abilityPermissionOf(ability.getIdentifier().asString(), state.getDisguiseIdentifier());
        return !player.isPermissionSet(singleAbilityPerm) || player.hasPermission(singleAbilityPerm);
    }
}
