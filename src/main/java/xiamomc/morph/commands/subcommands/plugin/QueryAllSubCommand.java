package xiamomc.morph.commands.subcommands.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;

public class QueryAllSubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public String getCommandName()
    {
        return "queryall";
    }

    @Override
    public String getHelpMessage()
    {
        return "列出所有正在伪装的玩家";
    }

    @Override
    public String getPermissionRequirement()
    {
        return "xiamomc.morph.query";
    }

    @Resolved
    private MorphManager manager;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull String[] strings)
    {
        var list = manager.getDisguisedPlayers();
        var offlineStates = manager.getAvaliableOfflineStates();

        if (list.size() == 0 && offlineStates.size() == 0)
        {
            commandSender.sendMessage(MessageUtils.prefixes(commandSender, Component.text("目前没有人伪装成任何东西")));
            return true;
        }

        for (var i : list)
        {
            var player = i.getPlayer();
            var msg = MessageUtils.prefixes(commandSender,
                    Component.text(player.getName() + (player.isOnline() ? "" : "（离线）") + " 伪装成了 ")
                            .append(i.getDisplayName())
            );

            if (i.showingDefaultItems())
                msg = msg.append(Component.translatable("（显示伪装装备）"));

            commandSender.sendMessage(msg);
        }

        for (var s : offlineStates)
        {
            commandSender.sendMessage(MessageUtils.prefixes(commandSender,
                    Component.text(s.playerName + "（离线存储） 伪装成了 " + s.disguiseID)));
        }

        return true;
    }
}
