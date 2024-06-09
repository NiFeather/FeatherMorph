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
import xiamomc.morph.misc.permissions.CommonPermissions;
import xiamomc.morph.utilities.BindableUtils;
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
        subCommands.add(getToggle("bossbar", ConfigOption.DISPLAY_BOSSBAR, CommandNameStrings.bossbarString()));

        subCommands.add(getToggle("unmorph_on_death", ConfigOption.UNMORPH_ON_DEATH));

        //subCommands.with(getToggle("allow_local_disguise", ConfigOption.ALLOW_LD_DISGUISES, "ld", CommandNameStrings.allowLDDisguiseString()));

        subCommands.add(getToggle("allow_client_mods", ConfigOption.ALLOW_CLIENT));

        subCommands.add(getToggle("piglin_brute_ignore_disguises", ConfigOption.PIGLIN_BRUTE_IGNORE_DISGUISES));

        subCommands.add(getToggle("headmorph", ConfigOption.ALLOW_HEAD_MORPH, CommandNameStrings.headMorphString()));

        subCommands.add(getToggle("chatoverride", ConfigOption.ALLOW_CHAT_OVERRIDE, CommandNameStrings.chatOverrideString()));

        subCommands.add(getToggle("modify_bounding_boxes", ConfigOption.MODIFY_BOUNDING_BOX));

        subCommands.add(getToggle("force_protocol_version", ConfigOption.FORCE_TARGET_VERSION));

        subCommands.add(getToggle("armorstand_show_arms", ConfigOption.ARMORSTAND_SHOW_ARMS));

        subCommands.add(getMirrorMode("mirror_mode", ConfigOption.MIRROR_SELECTION_MODE, null));
        subCommands.add(getToggle("mirror_interaction", ConfigOption.MIRROR_BEHAVIOR_DO_SIMULATION, CommandNameStrings.mirrorInteractionString()));
        subCommands.add(getToggle("mirror_sneak", ConfigOption.MIRROR_BEHAVIOR_SNEAK, CommandNameStrings.mirrorSneakString()));
        subCommands.add(getToggle("mirror_swaphand", ConfigOption.MIRROR_BEHAVIOR_SWAP_HAND, CommandNameStrings.mirrorSwapHandString()));
        subCommands.add(getToggle("mirror_drop", ConfigOption.MIRROR_BEHAVIOR_DROP, CommandNameStrings.mirrorDropString()));
        subCommands.add(getToggle("mirror_hotbar", ConfigOption.MIRROR_BEHAVIOR_HOTBAR, CommandNameStrings.mirrorHotbar()));
        subCommands.add(getToggle("mirror_ignore_disguised", ConfigOption.MIRROR_IGNORE_DISGUISED, CommandNameStrings.mirrorIgnoreDisguised()));
        subCommands.add(getToggle("mirror_log_operations", ConfigOption.MIRROR_LOG_OPERATION));
        subCommands.add(getInteger("mirror_log_cleanup", ConfigOption.MIRROR_LOG_CLEANUP_DATE));

        subCommands.add(getToggle("debug_output", ConfigOption.DEBUG_OUTPUT));
        subCommands.add(getToggle("revealing", ConfigOption.REVEALING));

        subCommands.add(getToggle("check_update", ConfigOption.CHECK_UPDATE));
        subCommands.add(getToggle("allow_acquire_morphs", ConfigOption.ALLOW_ACQUIRE_MORPHS));

        subCommands.add(getToggle("log_outgoing_packets", ConfigOption.LOG_OUTGOING_PACKETS));
        subCommands.add(getToggle("log_incoming_packets", ConfigOption.LOG_INCOMING_PACKETS));

        subCommands.add(getToggle("allow_acquire_morphs", ConfigOption.ALLOW_ACQUIRE_MORPHS));

        subCommands.add(getToggle("allow_flight", ConfigOption.ALLOW_FLIGHT));

        subCommands.add(getToggle("client_renderer", ConfigOption.USE_CLIENT_RENDERER));

        subCommands.add(getList("banned_disguises", ConfigOption.BANNED_DISGUISES, null));
        subCommands.add(getList("nofly_worlds", ConfigOption.NOFLY_WORLDS, null));
        subCommands.add(getList("blacklist_tags", ConfigOption.BLACKLIST_TAGS, null));
        subCommands.add(getList("blacklist_nbt_pattern", ConfigOption.BLACKLIST_PATTERNS, null));

        subCommands.add(getToggle("ability_check_permissions", ConfigOption.DO_CHECK_ABILITY_PERMISSIONS, null));
    }

    private ISubCommand getList(String optionName, ConfigOption option,
                                    @Nullable FormattableMessage displayName)
    {
        var targetDisplay = displayName == null ? new FormattableMessage(plugin, optionName) : displayName;

        var bindableList = config.getBindableList(String.class, option);

        return SubCommandGenerator.command()
                .setName(optionName)
                .setPerm(this.getPermissionRequirement())
                .setExec((sender, args) ->
                {
                    if (args.length < 1)
                    {
                        var displayValue = BindableUtils.bindableListToString(bindableList);
                        sender.sendMessage(MessageUtils.prefixes(sender,
                                CommandStrings.optionValueString()
                                        .withLocale(MessageUtils.getLocale(sender))
                                        .resolve("what", targetDisplay, null)
                                        .resolve("value", displayValue)));

                        return true;
                    }

                    if (args.length < 2)
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender,
                                CommandStrings.listNoEnoughArguments()
                                        .withLocale(MessageUtils.getLocale(sender))));

                        return true;
                    }

                    var operation = args[0];
                    if (operation.equalsIgnoreCase("add"))
                    {
                        var value = args[1];
                        try
                        {
                            bindableList.add(value);

                            //workaround: List的add方法传入非null时永远返回true
                            if (bindableList.contains(value))
                            {
                                sender.sendMessage(MessageUtils.prefixes(sender,
                                        CommandStrings.listAddSuccess()
                                                .withLocale(MessageUtils.getLocale(sender))
                                                .resolve("value", value)
                                                .resolve("option", optionName)));
                            }
                            else
                            {
                                sender.sendMessage(MessageUtils.prefixes(sender,
                                        CommandStrings.listAddFailUnknown()
                                                .withLocale(MessageUtils.getLocale(sender))
                                                .resolve("value", value)
                                                .resolve("option", optionName)));
                            }
                        }
                        catch (Throwable t)
                        {
                            sender.sendMessage(MessageUtils.prefixes(sender,
                                    CommandStrings.listAddFailUnknown()
                                            .withLocale(MessageUtils.getLocale(sender))
                                            .resolve("value", value)
                                            .resolve("option", optionName)));

                            logger.error("Error adding option to bindable list: " + t.getMessage());
                        }

                        return true;
                    }
                    else if (operation.equalsIgnoreCase("remove"))
                    {
                        var value = args[1];
                        var listChanged = bindableList.remove(value);

                        if (listChanged)
                        {
                            sender.sendMessage(MessageUtils.prefixes(sender,
                                    CommandStrings.listRemoveSuccess()
                                            .withLocale(MessageUtils.getLocale(sender))
                                            .resolve("value", value)
                                            .resolve("option", optionName)));
                        }
                        else
                        {
                            sender.sendMessage(MessageUtils.prefixes(sender,
                                    CommandStrings.listRemoveFailUnknown()
                                            .withLocale(MessageUtils.getLocale(sender))
                                            .resolve("value", value)
                                            .resolve("option", optionName)));
                        }

                        return true;
                    }
                    else
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender,
                                CommandStrings.unknownOperation()
                                .withLocale(MessageUtils.getLocale(sender))
                                .resolve("operation", operation)));

                        return true;
                    }
                });
    }

    private <T> ISubCommand getGeneric(String name, ConfigOption option,
                                       @Nullable FormattableMessage displayName, Class<T> targetClass,
                                       Function<String, T> func, String typeName)
    {
        return getGeneric(name, option, displayName, targetClass, func, new FormattableMessage(plugin, typeName));
    }

    private <T> ISubCommand getGeneric(String name, ConfigOption option,
                                   @Nullable FormattableMessage displayName, Class<T> targetClass,
                                   Function<String, T> func, FormattableMessage typeName)
    {
        var targetDisplay = displayName == null ? new FormattableMessage(plugin, name) : displayName;

        return SubCommandGenerator.command()
                .setName(name)
                .setPerm(this.getPermissionRequirement())
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

    private ISubCommand getMirrorMode(String name, ConfigOption option, @Nullable FormattableMessage displayName)
    {
        return getGeneric(name, option, displayName, String.class, enumName ->
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

    private ISubCommand getDouble(String name, ConfigOption option, @Nullable FormattableMessage displayName)
    {
        return getGeneric(name, option, displayName, Double.class, Double::parseDouble, TypesString.typeDouble());
    }

    private ISubCommand getInteger(String name, ConfigOption option)
    {
        return getInteger(name, option, null);
    }

    private ISubCommand getInteger(String name, ConfigOption option, @Nullable FormattableMessage displayName)
    {
        return getGeneric(name, option, displayName, Integer.class, Integer::parseInt, TypesString.typeInteger());
    }

    private ISubCommand getToggle(String name, ConfigOption option)
    {
        return getToggle(name, option, null);
    }

    private ISubCommand getToggle(String name, ConfigOption option, @Nullable FormattableMessage displayName)
    {
        return getGeneric(name, option, displayName, Boolean.class, this::parseBoolean, "true/false");
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
        return CommonPermissions.SET_OPTIONS;
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
