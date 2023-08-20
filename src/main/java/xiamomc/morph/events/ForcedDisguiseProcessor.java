package xiamomc.morph.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.events.api.gameplay.PlayerUnMorphEvent;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

public class ForcedDisguiseProcessor extends MorphPluginObject implements Listener
{
    public ForcedDisguiseProcessor()
    {
        forcedId.onValueChanged((o, n) ->
        {
            doForcedDisguise.set(!n.equals(MorphManager.forcedDisguiseNoneId));

            if (doForcedDisguise.get())
            {
                logger.info("Config changed, re-disguising players...");
                var players = Bukkit.getOnlinePlayers();
                players.forEach(p -> this.doDisguise(p, n));
            }
        }, true);

        config.bind(forcedId, ConfigOption.FORCED_DISGUISE);
    }

    private final Bindable<String> forcedId = new Bindable<>(MorphManager.forcedDisguiseNoneId);

    private final Bindable<Boolean> doForcedDisguise = new Bindable<>(false);

    @Resolved(shouldSolveImmediately = true)
    private MorphConfigManager config;

    @Resolved(shouldSolveImmediately = true)
    private MorphManager manager;

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        if (doForcedDisguise.get())
            doDisguise(e.getPlayer(), forcedId.get());
    }

    private void doDisguise(Player player, String targetId)
    {
        logger.info("Trying to disguise %s as %s".formatted(player.getName(), targetId));

        var success = manager.morph(Bukkit.getConsoleSender(), player, targetId,
                null,
                true, true);

        if (!success)
            logger.error("Unable to disguise %s as %s".formatted(player.getName(), targetId));
    }

    @EventHandler
    public void onUnmorph(PlayerUnMorphEvent e)
    {
        if (doForcedDisguise.get())
        {
            var player = e.getPlayer();

            logger.info("%s undisguised themselves, re-disguising...".formatted(player.getName()));
            doDisguise(player, forcedId.get());
        }
    }
}
