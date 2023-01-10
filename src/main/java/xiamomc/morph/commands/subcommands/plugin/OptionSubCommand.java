package xiamomc.morph.commands.subcommands.plugin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.commands.subcommands.SubCommandGenerator;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.CommandNameStrings;
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.HelpStrings;
import xiamomc.morph.messages.MessageUtils;
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

        subCommands.add(getToggle("allow_local_disguise", ConfigOption.ALLOW_LD_DISGUISES, "ld", CommandNameStrings.allowLDDisguiseString()));

        subCommands.add(getToggle("allow_client_mods", ConfigOption.ALLOW_CLIENT, "client"));

        subCommands.add(getToggle("piglin_brute_ignore_disguises", ConfigOption.PIGLIN_BRUTE_IGNORE_DISGUISES, "piglin_brute_ignore_disguises"));

        subCommands.add(getToggle("headmorph", ConfigOption.ALLOW_HEAD_MORPH, "headmorph", CommandNameStrings.headMorphString()));

        subCommands.add(getToggle("chatoverride", ConfigOption.ALLOW_CHAT_OVERRIDE, "chatoverride", CommandNameStrings.chatOverrideString()));

        subCommands.add(getToggle("armorstand_show_arms", ConfigOption.ARMORSTAND_SHOW_ARMS, "armorstand_show_arms"));

        subCommands.add(getToggle("reverse_interaction", ConfigOption.REVERSE_BEHAVIOR_DO_SIMULATION, "reverse.interaction", CommandNameStrings.reverseInteractionString()));
        subCommands.add(getToggle("reverse_sneak", ConfigOption.REVERSE_BEHAVIOR_SNEAK, "reverse.sneak", CommandNameStrings.reverseSneakString()));
        subCommands.add(getToggle("reverse_swaphand", ConfigOption.REVERSE_BEHAVIOR_SWAP_HAND, "reverse.swaphand", CommandNameStrings.reverseSwapHandString()));
        subCommands.add(getToggle("reverse_drop", ConfigOption.REVERSE_BEHAVIOR_DROP, "reverse.drop", CommandNameStrings.reverseDropString()));
        subCommands.add(getToggle("reverse_hotbar", ConfigOption.REVERSE_BEHAVIOR_HOTBAR, "reverse.hotbar", CommandNameStrings.reverseHotbar()));
        subCommands.add(getToggle("reverse_ignore_disguised", ConfigOption.REVERSE_IGNORE_DISGUISED, "reverse.ignore_disguised", CommandNameStrings.reverseIgnoreDisguised()));
    }

    private ISubCommand getToggle(String name, ConfigOption option, String perm)
    {
        return getToggle(name, option, perm, null);
    }

    private ISubCommand getToggle(String name, ConfigOption option, String perm, FormattableMessage displayName)
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
