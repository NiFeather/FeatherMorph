package xyz.nifeather.morph.events;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.misc.mobs.goal.handles.EntityGoalHandles;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

public class EntityProcessor extends MorphPluginObject implements Listener
{
    @Resolved(shouldSolveImmediately = true)
    private MorphConfigManager config;

    private final boolean doModifyAI;

    public boolean currentlyDoModifyAI()
    {
        return doModifyAI;
    }

    public EntityProcessor()
    {
        doModifyAI = config.get(Boolean.class, ConfigOption.DO_MODIFY_AI);

        config.getBindable(Boolean.class, ConfigOption.DO_MODIFY_AI).onValueChanged((o, n) ->
        {
            if (doModifyAI == n) return;

            logger.warn("- x - x - x - x - x - x - x - x - x - x - x - x -");
            logger.warn("");
            logger.warn("Changes were made about the option of modifying Mobs' AI.");
            logger.warn("And this requires a server restart!");
            logger.warn("");
            logger.warn("- x - x - x - x - x - x - x - x - x - x - x - x -");

            for (var player : featherMorph().getPlatform().onlinePlayersNative())
            {
                if (player.hasPermission(CommonPermissions.ADMIN))
                {
                    MessageUtils.send(player, CommandStrings.aiWarningPrimary());
                    MessageUtils.send(player, CommandStrings.aiWarningSecondary());
                }
            }
        });
    }

    @EventHandler
    public void onEntityAdded(EntityAddToWorldEvent e)
    {
        if (!doModifyAI) return;

        var entity = e.getEntity();
        if (!(entity instanceof Mob mob)) return;

        EntityGoalHandles.instance().handle(mob);
    }
}
