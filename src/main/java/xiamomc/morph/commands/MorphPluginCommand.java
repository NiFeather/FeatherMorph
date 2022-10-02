package xiamomc.morph.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.commands.subcommands.HelpSubCommand;
import xiamomc.morph.commands.subcommands.ISubCommand;
import xiamomc.morph.commands.subcommands.ReloadSubCommand;
import xiamomc.morph.misc.MessageUtils;
import xiamomc.pluginbase.Command.IPluginCommand;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MorphPluginCommand extends MorphPluginObject implements IPluginCommand {
    @Override
    public String getCommandName() {
        return "mmorph";
    }

    private final List<ISubCommand> subCommands = List.of(
            new ReloadSubCommand(),
            new HelpSubCommand()
    );

    private final List<String> emptyStringList = new ArrayList<>();

    private ISubCommand findSubCommandOrNull(String subCommandBaseName, List<ISubCommand> subCommands)
    {
        if (subCommands == null || subCommandBaseName == null) return null;

        var cmd = subCommands.stream()
                .filter(c -> c.getSubCommandName().equals(subCommandBaseName)).findFirst();

        return cmd.orElse(null);
    }

    @Override
    public List<String> onTabComplete(String baseName, String[] args, CommandSender source)
    {
        String subBaseName;

        subBaseName = args.length >= 1 ? args[0] : "";

        //匹配所有可用的子命令
        var avaliableSubCommands = new ArrayList<ISubCommand>();

        //只添加有权限的指令
        subCommands.forEach(c ->
        {
            var perm = c.getPermissionRequirement();

            if (perm == null || source.hasPermission(perm))
                avaliableSubCommands.add(c);
        });

        //Logger.warn("BUFFER: '" + Arrays.toString(args) + "'");

        //如果子命令baseName不为空
        if (args.length >= 2)
        {
            //查询匹配的子命令
            var subCommand = findSubCommandOrNull(subBaseName, avaliableSubCommands);

            //如果有
            if (subCommand != null)
                return subCommand.onTabComplete(ArrayUtils.remove(args, 0), source);

            return emptyStringList;
        }

        //否则，则返回所有可用的子命令
        var list = new ArrayList<String>();

        avaliableSubCommands.forEach(c ->
        {
            if (c.getSubCommandName().startsWith(subBaseName))
                list.add(c.getSubCommandName());
        });

        return list;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        String subBaseName;

        subBaseName = args.length >= 1 ? args[0] : "";

        var cmd = findSubCommandOrNull(subBaseName, subCommands);

        if (cmd != null)
            return cmd.onCommand(sender, args);
        else
            sender.sendMessage(MessageUtils.prefixes(Component.translatable("未找到改指令")).color(NamedTextColor.RED));

        return true;
    }
}
