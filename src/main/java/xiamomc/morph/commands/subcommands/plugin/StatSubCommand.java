package xiamomc.morph.commands.subcommands.plugin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.abilities.AbilityManager;
import xiamomc.morph.messages.HelpStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.StatStrings;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

public class StatSubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public @NotNull String getCommandName()
    {
        return "stat";
    }

    /**
     * 获取此指令的帮助信息
     *
     * @return 帮助信息
     */
    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.statDescription();
    }

    @Resolved
    private MorphClientHandler clientHandler;

    @Resolved
    private MorphSkillHandler skillHandler;

    @Resolved
    private AbilityManager abilityHandler;

    @Resolved
    private MorphManager morphManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        var disguisesActive = morphManager.getActiveDisguises().stream()
                .filter(s -> s.getPlayer().isOnline()).toArray().length;

        var authors = "MATRIX-feather"; //plugin.getPluginMeta().getAuthors();

        var listString = new StringBuilder();
        var backends = morphManager.listManagedBackends();
        var locale = MessageUtils.getLocale(sender);

        for (var backend : backends)
        {
            var instances = backend.listInstances();
            if (instances.isEmpty()) continue;

            var formattable = StatStrings.backendDescription();

            formattable.resolve("name", backend.getIdentifier())
                    .resolve("count", "" + instances.size());

            var str = formattable.toString(locale);
            listString.append(str).append(" ");
        }

        if (listString.isEmpty())
            listString.append(StatStrings.backendsNone().toString(locale));

        var defaultBackend = morphManager.getDefaultBackend();
        var defaultBackendString = "%s (%s)".formatted(defaultBackend.getIdentifier(), defaultBackend.getClass().getName());

        var msg = new FormattableMessage[]
                {
                        StatStrings.versionString()
                                .resolve("version", plugin.getPluginMeta().getVersion())
                                .resolve("author", authors)
                                .resolve("proto", String.valueOf(clientHandler.targetApiVersion)),

                        StatStrings.defaultBackendString()
                                        .resolve("backend", defaultBackendString),

                        StatStrings.activeBackends()
                                        .resolve("list", listString.toString()),

                        StatStrings.providersString()
                                .resolve("count", String.valueOf(MorphManager.getProviders().size())),

                        StatStrings.bannedDisguisesString()
                                .resolve("count", String.valueOf(morphManager.getBannedDisguises().size())),

                        StatStrings.abilitiesString()
                                .resolve("count", String.valueOf(abilityHandler.getRegistedAbilities().size())),

                        StatStrings.skillsString()
                                .resolve("count", String.valueOf(skillHandler.getRegistedSkills().size())),

                        StatStrings.activeClientsString()
                                .resolve("count", String.valueOf(clientHandler.getConnectedPlayers().size())),

                        StatStrings.activeDisguisesString()
                                .resolve("count", String.valueOf(disguisesActive))
                                .resolve("max", String.valueOf(Bukkit.getOnlinePlayers().size()))
                };

        for (FormattableMessage formattableMessage : msg)
            sender.sendMessage(MessageUtils.prefixes(sender, formattableMessage));

        return true;
    }

    private FormattableMessage getFormattable(String str)
    {
        return new FormattableMessage(plugin, str);
    }
    
    @Override
    public String getPermissionRequirement()
    {
        return "xiamomc.morph.stat";
    }
}
