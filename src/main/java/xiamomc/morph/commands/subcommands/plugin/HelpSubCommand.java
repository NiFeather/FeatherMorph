package xiamomc.morph.commands.subcommands.plugin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
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
import xiamomc.morph.messages.HelpStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Command.SubCommandHandler;
import xiamomc.pluginbase.Messages.FormattableMessage;

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

    private final List<Section> commandSections = new ObjectArrayList<>();

    /**
     * 设置用于构建帮助信息的Section
     */
    private void setupCommandSections()
    {
        //不属于任何section的指令丢到这里
        var miscCommandSection = new Section("/", HelpStrings.morphCommandDescription(), ObjectList.of(
                HelpStrings.morphCommandSpecialNote1(),
                HelpStrings.morphCommandSpecialNote2()
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

                List<FormattableMessage> notes = new ObjectArrayList<>(sch.getNotes());

                var section = new Section(parentCommandName,
                        sch.getHelpMessage(),
                        notes);

                //添加指令到section中
                for (var sc : sch.getSubCommands())
                {
                    var cmdName = parentCommandName + " " + sc.getCommandName() + " ";
                    section.add(new Entry(sc.getPermissionRequirement(),
                            cmdName,
                             sc.getHelpMessage(),
                            "/" + cmdName));
                }

                commandSections.add(section);
            }
            else
                miscCommandSection.add(new Entry(c.getPermissionRequirement(),
                        c.getCommandName(),
                        c.getHelpMessage(),
                        "/" + c.getCommandName() + " "));
        }
    }

    private List<Component> constructSectionMessage(CommandSender sender, Section section)
    {
        var entries = section.getEntries();

        //添加section的标题
        var list = ObjectArrayList.of(
                Component.empty(),
                HelpStrings.commandSectionHeaderString()
                        .resolve("basename", section.getCommandBaseName()).toComponent());

        //build entry
        for (var entry : entries)
        {
            var perm = entry.permission();

            //如果指令不要求权限或者sender拥有此权限，添加到列表里
            if (perm == null || sender.hasPermission(perm))
            {
                var msg = HelpStrings.commandEntryString()
                        .resolve("basename", entry.baseName())
                        .resolve("description", entry.description())
                        .toComponent()
                        .decorate(TextDecoration.UNDERLINED)
                        .hoverEvent(HoverEvent.showText(HelpStrings.clickToCompleteString().toComponent()))
                        .clickEvent(ClickEvent.suggestCommand(entry.suggestingCommand()));

                list.add(msg);
            }
        }

        if (section.getNotes() != null && section.getNotes().size() >= 1)
        {
            list.addAll(ObjectList.of(
                    Component.empty(),
                    HelpStrings.specialNoteString().toComponent()
            ));

            for (var f : section.getNotes())
            {
                list.add(f.toComponent()
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
        var list = new ObjectArrayList<Component>();

        list.add(HelpStrings.avaliableCommandHeaderString().toComponent());
        for (var section : commandSections)
        {
            var msg = HelpStrings.commandNamePatternString()
                    .resolve("basename", section.getCommandBaseName())
                    .resolve("description", section.getDescription())
                    .toComponent()
                    .decorate(TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.runCommand("/feathermorph " + getCommandName() + " " + section.getCommandBaseName()))
                    .hoverEvent(HoverEvent.showText(HelpStrings.clickToViewString().toComponent()));

            list.add(msg);
        }

        return list;
    }


    @Override
    public String getPermissionRequirement()
    {
        return null;
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.helpDescription();
    }

    @Override
    public @Nullable List<String> onTabComplete(List<String> args, CommandSender source)
    {
        var baseName = args.size() >= 1 ? args.get(0) : "";
        var matchedSections = commandSections.stream()
                .filter(s -> s.getCommandBaseName().toLowerCase().startsWith(baseName.toLowerCase())).toList();

        var list = new ObjectArrayList<String>();

        for (var s : matchedSections)
            list.add(s.getCommandBaseName());

        return list;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (args.length >= 1)
        {
            var section = commandSections.stream()
                    .filter(s -> s.getCommandBaseName().equalsIgnoreCase(args[0])).findFirst().orElse(null);

            if (section != null)
            {
                for (var s : constructSectionMessage(sender, section))
                    sender.sendMessage(MessageUtils.prefixes(sender, s));
            }
            else
                sender.sendMessage(MessageUtils.prefixes(sender, HelpStrings.sectionNotFoundString()));

            return true;
        }

        for (var s : constructHelpMessage(sender))
            sender.sendMessage(MessageUtils.prefixes(sender, s));

        return true;
    }
}
