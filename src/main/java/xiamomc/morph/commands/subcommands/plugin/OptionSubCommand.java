package xiamomc.morph.commands.subcommands.plugin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.commands.subcommands.SubCommandGenerator;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.*;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.List;

public class OptionSubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public @NotNull String getCommandName()
    {
        return "option";
    }

    @Resolved
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

        subCommands.add(getToggle("mirror_interaction", ConfigOption.MIRROR_BEHAVIOR_DO_SIMULATION, "mirror.interaction", CommandNameStrings.mirrorInteractionString()));
        subCommands.add(getToggle("mirror_sneak", ConfigOption.MIRROR_BEHAVIOR_SNEAK, "mirror.sneak", CommandNameStrings.mirrorSneakString()));
        subCommands.add(getToggle("mirror_swaphand", ConfigOption.MIRROR_BEHAVIOR_SWAP_HAND, "mirror.swaphand", CommandNameStrings.mirrorSwapHandString()));
        subCommands.add(getToggle("mirror_drop", ConfigOption.MIRROR_BEHAVIOR_DROP, "mirror.drop", CommandNameStrings.mirrorDropString()));
        subCommands.add(getToggle("mirror_hotbar", ConfigOption.MIRROR_BEHAVIOR_HOTBAR, "mirror.hotbar", CommandNameStrings.mirrorHotbar()));
        subCommands.add(getToggle("mirror_ignore_disguised", ConfigOption.MIRROR_IGNORE_DISGUISED, "mirror.ignore_disguised", CommandNameStrings.mirrorIgnoreDisguised()));
        subCommands.add(getToggle("mirror_log_operations", ConfigOption.MIRROR_LOG_OPERATION, "mirror.log_operations"));
        subCommands.add(getInteger("mirror_log_cleanup", ConfigOption.MIRROR_LOG_CLEANUP_DATE, "mirror.log_operations"));
        subCommands.add(getToggle("check_speeding", ConfigOption.CHECK_SPEEDING, "do_simple_anticheat"));
        subCommands.add(getToggle("debug_output", ConfigOption.DEBUG_OUTPUT, "debug_output"));
        subCommands.add(getToggle("revealing", ConfigOption.REVEALING, "revealing"));
    }

    private ISubCommand getInteger(String name, ConfigOption option, String perm)
    {
        return getInteger(name, option, perm, null);
    }

    private ISubCommand getInteger(String name, ConfigOption option, String perm, @Nullable FormattableMessage displayName)
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
                                        .resolve("value", config.get(Integer.class, option) + "")));

                        return true;
                    }

                    int value = -1;

                    try
                    {
                        value = Integer.parseInt(args[0]);
                    }
                    catch (Throwable ignored)
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender,
                                CommandStrings.argumentTypeErrorString()
                                        .withLocale(MessageUtils.getLocale(sender))
                                        .resolve("type", TypesString.typeInteger())));

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

    private ISubCommand getToggle(String name, ConfigOption option, String perm)
    {
        return getToggle(name, option, perm, null);
    }

    private ISubCommand getToggle(String name, ConfigOption option, String perm, @Nullable FormattableMessage displayName)
    {
        var targetDisplay = displayName == null ? new FormattableMessage(plugin, name) : displayName;

        return SubCommandGenerator.command()
                .setName(name)
                .setExec((sender, args) ->
                {
                    boolean newVal;

                    var locale = MessageUtils.getLocale(sender);
                    targetDisplay.withLocale(locale);

                    if (args.length >= 1)
                    {
                        var arg = args[0];

                        newVal = "true".equalsIgnoreCase(arg)
                                || "t".equalsIgnoreCase(arg)
                                || "on".equalsIgnoreCase(arg)
                                || "1".equalsIgnoreCase(arg)
                                || "enable".equalsIgnoreCase(arg)
                                || "enabled".equalsIgnoreCase(arg);
                    }
                    else
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender,
                                CommandStrings.optionValueString()
                                        .withLocale(locale)
                                        .resolve("what", targetDisplay, null)
                                        .resolve("value", config.get(Boolean.class, option) + "")));
                        return true;
                    }

                    config.set(option, newVal);

                    sender.sendMessage(MessageUtils.prefixes(sender,
                            CommandStrings.optionSetString()
                                    .withLocale(locale)
                                    .resolve("what", targetDisplay, null)
                                    .resolve("value", newVal + "")));
                    return true;
                })
                .setPerm("xiamomc.morph.toggle." + perm);
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
