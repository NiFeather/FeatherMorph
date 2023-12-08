package xiamomc.morph.commands.subcommands.plugin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.commands.subcommands.SubCommandGenerator;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.events.InteractionMirrorProcessor;
import xiamomc.morph.messages.*;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.List;
import java.util.function.Function;

public class OptionSubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public @NotNull String getCommandName()
    {
        return "option";
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphConfigManager config;

    public OptionSubCommand()
    {
        subCommands.add(getToggle("bossbar", ConfigOption.DISPLAY_BOSSBAR, "bossbar", CommandNameStrings.bossbarString()));

        subCommands.add(getToggle("unmorph_on_death", ConfigOption.UNMORPH_ON_DEATH, "unmorph_on_death"));

        //subCommands.with(getToggle("allow_local_disguise", ConfigOption.ALLOW_LD_DISGUISES, "ld", CommandNameStrings.allowLDDisguiseString()));

        subCommands.add(getToggle("allow_client_mods", ConfigOption.ALLOW_CLIENT, "client"));

        subCommands.add(getToggle("piglin_brute_ignore_disguises", ConfigOption.PIGLIN_BRUTE_IGNORE_DISGUISES, "piglin_brute_ignore_disguises"));

        subCommands.add(getToggle("headmorph", ConfigOption.ALLOW_HEAD_MORPH, "headmorph", CommandNameStrings.headMorphString()));

        subCommands.add(getToggle("chatoverride", ConfigOption.ALLOW_CHAT_OVERRIDE, "chatoverride", CommandNameStrings.chatOverrideString()));

        subCommands.add(getToggle("modify_bounding_boxes", ConfigOption.MODIFY_BOUNDING_BOX, "modify_boxes"));

        subCommands.add(getToggle("force_protocol_version", ConfigOption.FORCE_TARGET_VERSION, "force_protocol_version"));

        subCommands.add(getToggle("armorstand_show_arms", ConfigOption.ARMORSTAND_SHOW_ARMS, "armorstand_show_arms"));

        subCommands.add(getMirrorMode("mirror_mode", ConfigOption.MIRROR_SELECTION_MODE, "mirror_mode", null));
        subCommands.add(getToggle("mirror_interaction", ConfigOption.MIRROR_BEHAVIOR_DO_SIMULATION, "mirror.interaction", CommandNameStrings.mirrorInteractionString()));
        subCommands.add(getToggle("mirror_sneak", ConfigOption.MIRROR_BEHAVIOR_SNEAK, "mirror.sneak", CommandNameStrings.mirrorSneakString()));
        subCommands.add(getToggle("mirror_swaphand", ConfigOption.MIRROR_BEHAVIOR_SWAP_HAND, "mirror.swaphand", CommandNameStrings.mirrorSwapHandString()));
        subCommands.add(getToggle("mirror_drop", ConfigOption.MIRROR_BEHAVIOR_DROP, "mirror.drop", CommandNameStrings.mirrorDropString()));
        subCommands.add(getToggle("mirror_hotbar", ConfigOption.MIRROR_BEHAVIOR_HOTBAR, "mirror.hotbar", CommandNameStrings.mirrorHotbar()));
        subCommands.add(getToggle("mirror_ignore_disguised", ConfigOption.MIRROR_IGNORE_DISGUISED, "mirror.ignore_disguised", CommandNameStrings.mirrorIgnoreDisguised()));
        subCommands.add(getToggle("mirror_log_operations", ConfigOption.MIRROR_LOG_OPERATION, "mirror.log_operations"));
        subCommands.add(getInteger("mirror_log_cleanup", ConfigOption.MIRROR_LOG_CLEANUP_DATE, "mirror.log_operations"));

        subCommands.add(getToggle("debug_output", ConfigOption.DEBUG_OUTPUT, "debug_output"));
        subCommands.add(getToggle("revealing", ConfigOption.REVEALING, "revealing"));

        subCommands.add(getToggle("check_update", ConfigOption.CHECK_UPDATE, "check_update"));
        subCommands.add(getToggle("allow_acquire_morphs", ConfigOption.ALLOW_ACQUIRE_MORPHS, "allow_acquire_morphs"));

        subCommands.add(getToggle("log_outgoing_packets", ConfigOption.LOG_OUTGOING_PACKETS, "log_outgoing_packets"));
        subCommands.add(getToggle("log_incoming_packets", ConfigOption.LOG_INCOMING_PACKETS, "log_incoming_packets"));

        subCommands.add(getToggle("allow_acquire_morphs", ConfigOption.ALLOW_ACQUIRE_MORPHS, "allow_acquire_morphs"));

        subCommands.add(getToggle("allow_flight", ConfigOption.ALLOW_FLIGHT, "allow_flight"));

        subCommands.add(getToggle("client_renderer", ConfigOption.USE_CLIENT_RENDERER, "client_renderer"));

        subCommands.add(getList("banned_disguises", ConfigOption.BANNED_DISGUISES, "banned_disguises", null));
    }

    private <T> ISubCommand getGeneric(String name, ConfigOption option, String perm,
                                       @Nullable FormattableMessage displayName, Class<T> targetClass,
                                       Function<String, T> func, String typeName)
    {
        return getGeneric(name, option, perm,
                displayName, targetClass, func, new FormattableMessage(plugin, typeName));
    }

    private ISubCommand getList(String optionName, ConfigOption option, String perm,
                                    @Nullable FormattableMessage displayName)
    {
        var targetDisplay = displayName == null ? new FormattableMessage(plugin, optionName) : displayName;

        var bindableList = config.getBindableList(String.class, option);

        return SubCommandGenerator.command()
                .setName(optionName)
                .setPerm("xiamomc.morph.toggle." + perm)
                .setExec((sender, args) ->
                {
                    if (args.length < 1)
                    {
                        var displayValue = bindableList.toString();
                        sender.sendMessage(MessageUtils.prefixes(sender,
                                CommandStrings.optionValueString()
                                        .withLocale(MessageUtils.getLocale(sender))
                                        .resolve("what", targetDisplay, null)
                                        .resolve("value", displayValue)));

                        return true;
                    }

                    if (args.length < 2)
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender, "参数数量不对"));
                        return true;
                    }

                    var operation = args[0];
                    if (operation.equalsIgnoreCase("add"))
                    {
                        var value = args[1];
                        try
                        {
                            bindableList.add(value);

                            //bug: bindableList的add和remove方法永远返回true
                            if (bindableList.contains(value))
                                sender.sendMessage(MessageUtils.prefixes(sender, "成功添加" + value));
                            else
                                sender.sendMessage(MessageUtils.prefixes(sender, "未能添加" + value + ", 可能是类型不对"));
                        }
                        catch (Throwable t)
                        {
                            sender.sendMessage(MessageUtils.prefixes(sender, "未能添加" + value + ", 可能是类型不对"));
                            logger.error("Error adding option to bindable list: " + t.getMessage());
                        }

                        return true;
                    }
                    else if (operation.equalsIgnoreCase("remove"))
                    {
                        var value = args[1];
                        bindableList.remove(value);

                        if (!bindableList.contains(value))
                            sender.sendMessage(MessageUtils.prefixes(sender, "成功移除" + value));
                        else
                            sender.sendMessage(MessageUtils.prefixes(sender, "未能移除" + value + ", 可能是其不在列表中"));

                        return true;
                    }
                    else
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender, "未知操作: " + operation));
                        return true;
                    }
                });
    }

    private <T> ISubCommand getGeneric(String name, ConfigOption option, String perm,
                                   @Nullable FormattableMessage displayName, Class<T> targetClass,
                                   Function<String, T> func, FormattableMessage typeName)
    {
        var targetDisplay = displayName == null ? new FormattableMessage(plugin, name) : displayName;

        return SubCommandGenerator.command()
                .setName(name)
                .setPerm("xiamomc.morph.toggle." + perm)
                .setExec((sender, args) ->
                {
                    if (args.length < 1)
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender,
                                CommandStrings.optionValueString()
                                        .withLocale(MessageUtils.getLocale(sender))
                                        .resolve("what", targetDisplay, null)
                                        .resolve("value", config.get(targetClass, option) + "")));

                        return true;
                    }

                    T value = null;

                    try
                    {
                        value = func.apply(args[0]);

                        if (value == null)
                            throw new NullDependencyException("");
                    }
                    catch (Throwable ignored)
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender,
                                CommandStrings.argumentTypeErrorString()
                                        .withLocale(MessageUtils.getLocale(sender))
                                        .resolve("type", typeName)));

                        return true;
                    }

                    config.set(option, value);

                    sender.sendMessage(MessageUtils.prefixes(sender,
                            CommandStrings.optionSetString()
                                    .withLocale(MessageUtils.getLocale(sender))
                                    .resolve("what", targetDisplay, null)
                                    .resolve("value", value + "")));
                    return true;
                });
    }

    private ISubCommand getMirrorMode(String name, ConfigOption option, String perm, @Nullable FormattableMessage displayName)
    {
        return getGeneric(name, option, perm, displayName, String.class, enumName ->
        {
            String value = null;

            var byName = InteractionMirrorProcessor.InteractionMirrorSelectionMode.BY_NAME.toLowerCase();
            var bySight = InteractionMirrorProcessor.InteractionMirrorSelectionMode.BY_SIGHT.toLowerCase();

            if (enumName.equalsIgnoreCase(byName))
                value = byName;
            else if (enumName.equalsIgnoreCase(bySight))
                value = bySight;

            return value;
        }, "by_sight/by_name");
    }

    private ISubCommand getDouble(String name, ConfigOption option, String perm, @Nullable FormattableMessage displayName)
    {
        return getGeneric(name, option, perm, displayName, Double.class, Double::parseDouble, TypesString.typeDouble());
    }

    private ISubCommand getInteger(String name, ConfigOption option, String perm)
    {
        return getInteger(name, option, perm, null);
    }

    private ISubCommand getInteger(String name, ConfigOption option, String perm, @Nullable FormattableMessage displayName)
    {
        return getGeneric(name, option, perm, displayName, Integer.class, Integer::parseInt, TypesString.typeInteger());
    }

    private ISubCommand getToggle(String name, ConfigOption option, String perm)
    {
        return getToggle(name, option, perm, null);
    }

    private ISubCommand getToggle(String name, ConfigOption option, String perm, @Nullable FormattableMessage displayName)
    {
        return getGeneric(name, option, perm, displayName, Boolean.class, this::parseBoolean, "true/false");
    }

    private boolean parseBoolean(String input)
    {
        return "true".equalsIgnoreCase(input)
                || "t".equalsIgnoreCase(input)
                || "on".equalsIgnoreCase(input)
                || "1".equalsIgnoreCase(input)
                || "enable".equalsIgnoreCase(input)
                || "enabled".equalsIgnoreCase(input);
    }

    private final List<ISubCommand> subCommands = new ObjectArrayList<>();

    @Override
    public String getPermissionRequirement()
    {
        return "xiamomc.morph.toggle";
    }

    @Override
    public List<ISubCommand> getSubCommands()
    {
        return subCommands;
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.pluginOptionDescription();
    }
}
