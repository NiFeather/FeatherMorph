package xiamomc.morph.commands.subcommands.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

public class HelpSubCommand extends MorphPluginObject implements ISubCommand
{

    @Override
    public String getCommandName()
    {
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
        var miscCommandSection = new Section("/", "/ -- 伪装、取消伪装", List.of(
                "伪装可以通过击杀生物或玩家获得",
                "伪装时会优先复制视线方向5格以内的相同生物或玩家进行伪装"
        ));

        commandSections.add(miscCommandSection);

        //遍历所有指令
        for (var c : cmdHelper.getCommands())
        {
            //如果指令拥有子指令，新建section
            if (c instanceof SubCommandHandler<?> sch)
            {
                //此section下所有指令的父级指令
                var parentCommandName = sch.getCommandName();
                var section = new Section(parentCommandName,
                        "/" + parentCommandName + " ... -- " + sch.getHelpMessage(),
                        sch.getNotes());

                //添加指令到section中
                for (var sc : sch.getSubCommands())
                {
                    var cmd = "/" + parentCommandName + " " + sc.getCommandName() + " ";
                    section.add(new Entry(sc.getPermissionRequirement(),
                             cmd + " : " + sc.getHelpMessage(),
                            cmd));
                }

                commandSections.add(section);
            }
            else
                miscCommandSection.add(new Entry(c.getPermissionRequirement(),
                        "/" + c.getCommandName() + " : " + c.getHelpMessage(),
                        "/" + c.getCommandName() + " "));
        }
    }

    private List<Component> constructSectionMessage(CommandSender sender, Section section)
    {

        var entries = section.getEntries();

        //添加section的标题
        var list = new ArrayList<Component>(List.of(
                Component.empty(),
                Component.text("指令 ")
                        .append(Component.text("/" + section.getCommandBaseName())
                                .decorate(TextDecoration.ITALIC)
                                .hoverEvent(HoverEvent.showText(Component.text(section.getHeader()))))
                        .append(Component.text(" 的用法："))
        ));

        //build entry
        for (var entry : entries)
        {
            var perm = entry.permission();

            //如果指令不要求权限或者sender拥有此权限，添加到列表里
            if (perm == null || sender.hasPermission(perm))
                list.add(Component.text(entry.message())
                        .decorate(TextDecoration.UNDERLINED)
                        .hoverEvent(HoverEvent.showText(Component.text("点击补全")))
                        .clickEvent(ClickEvent.suggestCommand(entry.suggestingCommand())));
        }

        if (section.getFooter() != null && section.getFooter().size() >= 1)
        {
            list.addAll(List.of(
                    Component.empty(),
                    Component.text("特别标注：")
            ));

            for (var f : section.getFooter())
            {
                list.add(Component.text(f)
                        .decorate(TextDecoration.ITALIC));
            }
        }

        list.add(Component.empty());

        return list;
    }

    /**
     * 从设置的Section中构建sender的帮助信息
     *
     * @param sender 要显示给谁
     * @return 构建的帮助信息
     */
    private List<Component> constructHelpMessage(CommandSender sender)
    {
        var list = new ArrayList<Component>();

        list.add(Component.text("当前可用的指令（单击补全/查看）："));
        for (var section : commandSections)
        {
            list.add(Component.text(section.getHeader())
                    .decorate(TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/mmorph " + getCommandName() + " " + section.getCommandBaseName()))
                    .hoverEvent(HoverEvent.showText(Component.text("点击查看"))));
        }

        return list;
    }


    @Override
    public String getPermissionRequirement()
    {
        return null;
    }

    @Override
    public String getHelpMessage()
    {
        return "显示帮助";
    }

    @Override
    public @Nullable List<String> onTabComplete(List<String> args, CommandSender source)
    {
        var baseName = args.size() >= 1 ? args.get(0) : "";
        var matchedSections = commandSections.stream()
                .filter(s -> s.getCommandBaseName().toLowerCase().startsWith(baseName.toLowerCase())).toList();

        var list = new ArrayList<String>();

        for (var s : matchedSections)
            list.add(s.getCommandBaseName());

        return list;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (args.length >= 1)
        {
            var matchedSection = commandSections.stream()
                    .filter(s -> s.getCommandBaseName().equalsIgnoreCase(args[0])).findFirst();

            if (matchedSection.isPresent())
            {
                for (var s : constructSectionMessage(sender, matchedSection.get()))
                    sender.sendMessage(MessageUtils.prefixes(sender, s));
            }
            else
                sender.sendMessage(MessageUtils.prefixes(sender, Component.translatable("未找到此章节").color(NamedTextColor.RED)));

            return true;
        }

        for (var s : constructHelpMessage(sender))
            sender.sendMessage(MessageUtils.prefixes(sender, s));

        return true;
    }
}
