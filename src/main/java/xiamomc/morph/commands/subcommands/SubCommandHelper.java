package xiamomc.morph.commands.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.misc.MessageUtils;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.PluginObject;
import xiamomc.pluginbase.XiaMoJavaPlugin;

import java.util.ArrayList;
import java.util.List;

public abstract class SubCommandHelper<T extends XiaMoJavaPlugin> extends PluginObject<T> implements IPluginCommand
{
    protected abstract List<ISubCommand> getSubCommands();

    private ISubCommand findSubCommandOrNull(String subCommandBaseName, List<ISubCommand> subCommands)
    {
        if (subCommands == null || subCommandBaseName == null) return null;

        var cmd = subCommands.stream()
                .filter(c -> c.getSubCommandName().equals(subCommandBaseName)).findFirst();

        return cmd.orElse(null);
    }

    private final List<String> emptyStringList = new ArrayList<>();

    public List<String> onTabComplete(String baseName, String[] args, CommandSender source)
    {
        String subBaseName;

        subBaseName = args.length >= 1 ? args[0] : "";

        //匹配所有可用的子命令
        var avaliableSubCommands = new ArrayList<ISubCommand>();

        //只添加有权限的指令
        getSubCommands().forEach(c ->
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
            {
                var result = subCommand.onTabComplete(ArrayUtils.remove(args, 0), source);

                return result == null ? emptyStringList : result;
            }

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

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        String subBaseName;

        subBaseName = args.length >= 1 ? args[0] : "";

        var cmd = findSubCommandOrNull(subBaseName, getSubCommands());

        if (cmd != null)
            return cmd.onCommand(sender, args);
        else
            sender.sendMessage(MessageUtils.prefixes(Component.translatable("未找到改指令")).color(NamedTextColor.RED));

        return true;
    }
}
