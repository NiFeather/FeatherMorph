package xiamomc.morph.commands.subcommands.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.commands.MorphCommandHelper;
import xiamomc.morph.commands.subcommands.plugin.helpsections.Entry;
import xiamomc.morph.commands.subcommands.plugin.helpsections.Section;
import xiamomc.morph.misc.MessageUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Command.SubCommandHandler;

import java.util.ArrayList;
import java.util.List;

public class HelpSubCommand extends MorphPluginObject implements ISubCommand {

    @Override
    public String getCommandName() {
        return "help";
    }

    @Initializer
    private void load()
    {
        setupCommandSections();
    }

    @Resolved
    private MorphCommandHelper cmdHelper;

    private final List<Section> commandSections = new ArrayList<>();

    /**
     * 设置用于构建帮助信息的Section
     */
    private void setupCommandSections()
    {
        //不属于任何section的指令丢到这里
        var miscCommandSection = new Section("/ -- 插件指令");

        commandSections.add(miscCommandSection);

        //遍历所有指令
        for (var c : cmdHelper.getCommands())
        {
            //如果指令拥有子指令，新建section
            if (c instanceof SubCommandHandler<?> sch)
            {
                //此section下所有指令的父级指令
                var parentCommandName =  sch.getCommandName();
                var section = new Section("/" + sch.getCommandName() + " -- " + sch.getHelpMessage());

                //添加指令到section中
                for (var sc : sch.getSubCommands())
                {
                    section.add(new Entry(sc.getPermissionRequirement(),
                            "/" + parentCommandName + " " + sc.getCommandName() + " : " + sc.getHelpMessage()));
                }

                commandSections.add(section);
            }
            else
                miscCommandSection.add(new Entry(c.getPermissionRequirement(), "/" + c.getCommandName() + " : " + c.getHelpMessage()));
        }
    }

    /**
     * 从设置的Section中构建sender的帮助信息
     * @param sender 要显示给谁
     * @return 构建的帮助信息
     */
    private List<String> constructHelpMessage(CommandSender sender)
    {
        var list = new ArrayList<String>();
        var isFirstSection = true;

        var speractor = "-------------------------";

        for (var section : commandSections)
        {
            var entries = section.getEntries();

            //if (entries.stream()
            //        .noneMatch(e -> e.getPermission() != null && sender.hasPermission(e.getPermission()))) continue;

            //如果是第一个section，则额外添加一行分割线
            if (isFirstSection)
            {
                list.add(speractor);
                isFirstSection = false;
            }

            //添加section的标题
            list.add(section.getHeader());

            for (var entry : entries)
            {
                var perm = entry.permission();

                //如果指令不要求权限或者sender拥有此权限，添加到列表里
                if (perm == null) list.add(entry.message());
                else if (sender.hasPermission(perm)) list.add(entry.message());
            }

            list.add(speractor);
        }

        return list;
    }


    @Override
    public String getPermissionRequirement() {
        return null;
    }

    @Override
    public String getHelpMessage() {
        return "显示帮助";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        for (var s : constructHelpMessage(sender))
            sender.sendMessage(MessageUtils.prefixes(sender, Component.text(s)));

        return true;
    }
}
