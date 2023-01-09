package xiamomc.morph.abilities.impl;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.abilities.options.BossbarOption;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.DisguiseUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.List;
import java.util.Objects;

public class BossbarAbility extends MorphAbility<BossbarOption>
{
    private Bindable<Boolean> allowBossbar;

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        allowBossbar = configManager.getBindable(Boolean.class, ConfigOption.DISPLAY_BOSSBAR);
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.BOSSBAR;
    }

    @Override
    protected BossbarOption createOption()
    {
        return new BossbarOption();
    }

    @Override
    public boolean revokeFromPlayer(Player player, DisguiseState state)
    {
        if (super.revokeFromPlayer(player, state))
        {
            state.setBossbar(null);
            return true;
        }

        return false;
    }

    @Override
    public boolean applyToPlayer(Player player, DisguiseState state)
    {
        if (super.applyToPlayer(player, state))
        {
            if (!allowBossbar.get()) return true;

            var option = getOr(
                    options.get(state.getDisguiseIdentifier()),
                    Objects::nonNull,
                    options.get(state.getSkillLookupIdentifier())
            );

            if (option == null) return false;

            var createOption = option.getCreateOption();

            state.setBossbar(BossBar.bossBar(
                    this.getBossbarName(state, option),
                    1f,
                    createOption.color(),
                    createOption.overlay(),
                    createOption.flags()
            ));
        }

        return false;
    }

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        if (super.handle(player, state))
        {
            if (appliedPlayers.contains(player))
            {
                var option = getOr(
                        options.get(state.getDisguiseIdentifier()),
                        Objects::nonNull,
                        options.get(state.getSkillLookupIdentifier())
                );

                if (option == null) return false;

                var bossbar = state.getBossbar();
                if (bossbar != null)
                {
                    var distance = option.getApplyDistance();

                    if (distance < 0)
                        distance = (Bukkit.getViewDistance() - 1) * 16;

                    var playerGameMode = player.getGameMode();
                    List<Player> playersToShow = DisguiseUtils.findNearbyPlayers(player, distance, true);
                    List<Player> playersToHide = new ObjectArrayList<>(Bukkit.getOnlinePlayers());

                    if (playerGameMode == GameMode.SPECTATOR)
                        playersToShow.removeIf(p -> p.getGameMode() != playerGameMode);

                    bossbar.progress((float) (player.getHealth() / player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
                    //bossbar.name(this.getBossbarName(state, option));

                    playersToHide.removeAll(playersToShow);
                    playersToHide.remove(player);

                    playersToShow.forEach(p -> p.showBossBar(bossbar));
                    playersToHide.forEach(p -> p.hideBossBar(bossbar));
                }

                return true;
            }
        }

        return false;
    }

    private Component getBossbarName(DisguiseState state, BossbarOption option)
    {
        return MiniMessage.miniMessage().deserialize(option.getCreateOption().name(),
                Placeholder.component("name", state.getDisplayName()),
                Placeholder.component("who", state.getPlayer().displayName()));
    }
}
