package xiamomc.morph.commands.subcommands.plugin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.abilities.AbilityHandler;
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
    private AbilityHandler abilityHandler;

    @Resolved
    private MorphManager morphManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        var disguisesActive = morphManager.getDisguiseStates().stream()
                .filter(s -> s.getPlayer().isOnline()).toArray().length;

        var authors = "MATRIX-feather"; //plugin.getPluginMeta().getAuthors();

        var list = new StringBuilder();
        var backends = morphManager.listManagedBackends();
        for (var backend : backends)
        {
            var instances = backend.listInstances();
            if (instances.isEmpty()) continue;

            var formattable = StatStrings.backendDescription();

            formattable.resolve("name", backend.getIdentifier())
                    .resolve("count", "" + instances.size());

            var locale = MessageUtils.getLocale(sender);
            var str = formattable.toString(locale);
            list.append(str).append(" ");
        }

        var msg = new FormattableMessage[]
                {
                        StatStrings.versionString()
                                .resolve("version", plugin.getPluginMeta().getVersion())
                                .resolve("author", authors)
                                .resolve("proto", String.valueOf(clientHandler.targetApiVersion)),

                        StatStrings.activeBackends()
                                        .resolve("list", list.toString()),

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
