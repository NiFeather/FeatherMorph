package xiamomc.morph.commands.subcommands.plugin;

import com.google.protobuf.Message;
import me.libraryaddict.disguise.DisguiseAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;

public class QuerySubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public String getCommandName() {
        return "query";
    }

    @Override
    public String getHelpMessage() {
        return "检查某个玩家是否在伪装";
    }

    @Override
    public String getPermissionRequirement()
    {
        return "xiamomc.morph.query";
    }

    @Resolved
    private MorphManager manager;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull String[] args)
    {
        //todo: can be removed when bumping PluginBase to 0.0.6
        if(!commandSender.hasPermission(getPermissionRequirement())) return false;

        if (args.length >= 1)
        {
            var targetPlayer = Bukkit.getPlayer(args[0]);

            if (targetPlayer != null)
            {
                var info = manager.getPlayerDisguisingInfo(targetPlayer);

                if (info != null)
                    commandSender.sendMessage(MessageUtils.prefixes(
                            Component.text(targetPlayer.getName() + " 正伪装为 " + info.displayName)
                    ));
                else if (DisguiseAPI.isDisguised(targetPlayer))
                {
                    commandSender.sendMessage(MessageUtils.prefixes(
                            Component.text(targetPlayer.getName() + " 正伪装为 "
                                    + DisguiseAPI.getDisguise(targetPlayer).getDisguiseName() + "（无法由Morph管理的伪装）")
                    ));
                }
            }
        }
        return true;
    }
}
