package xyz.nifeather.morph.misc.integrations.towny;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.messages.CommandNameStrings;
import xyz.nifeather.morph.messages.CommandStrings;
import xyz.nifeather.morph.messages.CommonStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

import java.util.List;

public class TownyToggleFlightCommand extends MorphPluginObject implements IPluginCommand
{
    private final TownyAdapter adapter;

    public TownyToggleFlightCommand(TownyAdapter adapter)
    {
        this.adapter = adapter;
    }

    @Override
    public String getCommandName()
    {
        return "toggle-town-morph-flight";
    }

    @Override
    public String getPermissionRequirement()
    {
        return CommonPermissions.TOGGLE_TOWN_FLIGHT;
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return new FormattableMessage(plugin, "Toggle town flight");
    }

    private final List<String> validOptions = List.of("on", "off");

    @Override
    public List<String> onTabComplete(List<String> args, CommandSender source)
    {
        if (args.size() > 1) return List.of();

        var argZero = args.isEmpty() ? "" : args.get(0);
        var zeroFinal = argZero.toUpperCase();

        return validOptions.stream().filter(str -> str.toUpperCase().startsWith(zeroFinal)).toList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String baseName, @NotNull String[] args)
    {
        if (!(sender instanceof Player player))
            return false;

        var towny = TownyAPI.getInstance();
        var town = towny.getTown(player);
        var resident = towny.getResident(player);

        if (town == null)
        {
            sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.townyDoesntHaveTown()));
            return true;
        }

        if (resident == null)
        {
            sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.unknownError()));
            return true;
        }

        if (!town.isMayor(resident))
        {
            sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.townyPlayerNotMayor()));
            return true;
        }

        var playerLocale = MessageUtils.getLocale(sender);
        var message = CommandStrings.optionValueString()
                .resolve("what", CommandNameStrings.morphFlightForTownX()
                        .withLocale(playerLocale)
                        .resolve("which", town.getName()));

        boolean allow;

        if (args.length < 1)
        {
            boolean currentAllow = true;

            var bdf = TownyAdapter.allowMorphFlight;
            if (MetaDataUtil.hasMeta(town, bdf))
                currentAllow = !MetaDataUtil.getBoolean(town, bdf);

            allow = currentAllow;
        }
        else
        {
            allow = parse(args[0]);
        }

        setTownFlightStatus(town, allow);

        message.resolve("value", (allow ? CommonStrings.on() : CommonStrings.off()).withLocale(playerLocale));

        sender.sendMessage(MessageUtils.prefixes(sender, message));

        return true;
    }

    private boolean parse(String str)
    {
        if (str.equalsIgnoreCase("on")) return true;
        else if (str.equalsIgnoreCase("off")) return false;
        else return Boolean.parseBoolean(str);
    }

    private void setTownFlightStatus(Town town, boolean newStatus)
    {
        MetaDataUtil.setBoolean(town, TownyAdapter.allowMorphFlight, newStatus, true);

        refreshPlayersIn(town);
    }

    private void refreshPlayersIn(Town town)
    {
        // Towny没有API来告诉我们一个Town里进了多少玩家
        // 因此我们只能遍历所有玩家实例
        Bukkit.getOnlinePlayers().forEach(player ->
        {
            // 获取玩家爱所在的Town
            var currentTown = TownyAPI.getInstance().getTown(player.getLocation());

            // 在野外或者不是目标town
            if (currentTown == null || currentTown != town) return;

            adapter.updatePlayer(player, currentTown);
        });
    }
}
