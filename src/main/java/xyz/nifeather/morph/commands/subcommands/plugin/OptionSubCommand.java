package xyz.nifeather.morph.commands.subcommands.plugin;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Configuration.ConfigOption;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.commands.brigadier.IConvertibleBrigadier;
import xyz.nifeather.morph.commands.subcommands.OptionSubCommands;
import xyz.nifeather.morph.config.ConfigOptions;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.messages.strings.CommandNameStrings;
import xyz.nifeather.morph.messages.strings.HelpStrings;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OptionSubCommand extends MorphPluginObject implements IConvertibleBrigadier
{
    @Override
    public @NotNull String name()
    {
        return "option";
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphConfigManager config;

    public OptionSubCommand()
    {
        subCommands.add(getToggle("bossbar", ConfigOptions.DISPLAY_BOSSBAR, CommandNameStrings.bossbarString()));

        subCommands.add(getToggle("unmorph_on_death", ConfigOptions.UNMORPH_ON_DEATH));

        //subCommands.with(getToggle("allow_local_disguise", ConfigOptions.ALLOW_LD_DISGUISES, "ld", CommandNameStrings.allowLDDisguiseString()));

        subCommands.add(getToggle("allow_client_mods", ConfigOptions.ALLOW_CLIENT));

        subCommands.add(getToggle("piglin_brute_ignore_disguises", ConfigOptions.PIGLIN_BRUTE_IGNORE_DISGUISES));

        subCommands.add(getToggle("headmorph", ConfigOptions.ALLOW_HEAD_MORPH, CommandNameStrings.headMorphString()));

        subCommands.add(getToggle("chatoverride", ConfigOptions.ALLOW_CHAT_OVERRIDE, CommandNameStrings.chatOverrideString()));

        subCommands.add(getToggle("modify_bounding_boxes", ConfigOptions.MODIFY_BOUNDING_BOX));

        subCommands.add(getToggle("force_protocol_version", ConfigOptions.FORCE_TARGET_VERSION));

        subCommands.add(getToggle("armorstand_show_arms", ConfigOptions.ARMORSTAND_SHOW_ARMS));

        subCommands.add(getToggle("debug_output", ConfigOptions.DEBUG_OUTPUT));
        subCommands.add(getToggle("revealing", ConfigOptions.REVEALING));

        subCommands.add(getToggle("check_update", ConfigOptions.CHECK_UPDATE));
        subCommands.add(getToggle("allow_acquire_morphs", ConfigOptions.ALLOW_ACQUIRE_MORPHS));

        subCommands.add(getToggle("log_outgoing_packets", ConfigOptions.LOG_OUTGOING_PACKETS));
        subCommands.add(getToggle("log_incoming_packets", ConfigOptions.LOG_INCOMING_PACKETS));

        subCommands.add(getToggle("allow_acquire_morphs", ConfigOptions.ALLOW_ACQUIRE_MORPHS));

        subCommands.add(getToggle("allow_flight", ConfigOptions.ALLOW_FLIGHT));

        subCommands.add(getToggle("client_renderer", ConfigOptions.USE_CLIENT_RENDERER));

        subCommands.add(getList("banned_disguises", ConfigOptions.BANNED_DISGUISES, null));
        subCommands.add(getList("nofly_worlds", ConfigOptions.NOFLY_WORLDS, null));
        //subCommands.add(getList("blacklist_tags", ConfigOptions.BLACKLIST_TAGS, null));
        //subCommands.add(getList("blacklist_nbt_pattern", ConfigOptions.BLACKLIST_PATTERNS, null));
        //subCommands.add(getList("disabled_worlds", ConfigOptions.DISGUISE_DISABLED_WORLDS, null));

        subCommands.add(getToggle("ability_check_permissions", ConfigOptions.DO_CHECK_ABILITY_PERMISSIONS, null));

        subCommands.add(getToggle("towny_allow_fly_in_wilderness", ConfigOptions.TOWNY_ALLOW_FLY_IN_WILDERNESS));
    }

    private IConvertibleBrigadier getList(String optionName, ConfigOption<List<String>> option,
                                                                                 @Nullable FormattableMessage displayName)
    {
        return new OptionSubCommands.StringListOptionBaseCommand(optionName, config, option);
    }

    private IConvertibleBrigadier getInteger(String name, ConfigOption<Integer> option)
    {
        return getInteger(name, option, null);
    }

    private IConvertibleBrigadier getInteger(String name, ConfigOption<Integer> option, @Nullable FormattableMessage displayName)
    {
        return new OptionSubCommands.IntegerOptionCommand(name, config, option).min(-1);
    }

    private IConvertibleBrigadier getToggle(String name, ConfigOption<Boolean> option)
    {
        return getToggle(name, option, null);
    }

    private IConvertibleBrigadier getToggle(String name, ConfigOption<Boolean> option, @Nullable FormattableMessage displayName)
    {
        return new OptionSubCommands.BooleanOptionCommand(name, config, option);
    }

    private final List<IConvertibleBrigadier> subCommands = new CopyOnWriteArrayList<>();

    @Override
    public @Nullable String permission()
    {
        return CommonPermissions.SET_OPTIONS;
    }

    @Override
    public void registerAsChild(ArgumentBuilder<CommandSourceStack, ?> parentBuilder)
    {
        var thisBuilder = Commands.literal(name()).requires(this::checkPermission);

        for (IConvertibleBrigadier subCommand : this.subCommands)
            subCommand.registerAsChild(thisBuilder);

        parentBuilder.then(thisBuilder);
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.pluginOptionDescription();
    }
}
