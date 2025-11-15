package xyz.nifeather.morph.commands.subcommands.plugin.extract;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.messages.strings.TypesString;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.utilities.PluginAssetUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DumpLanguageCommand extends BrigadierCommand
{
    @Override
    public @Nullable String getPermissionRequirement()
    {
        return CommonPermissions.ADMIN;
    }

    @Override
    public @NotNull String name()
    {
        return "language";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return null;
    }

    @Override
    public void registerAsChild(ArgumentBuilder<CommandSourceStack, ?> parentBuilder)
    {
        parentBuilder.then(
                Commands.literal(name())
                        .requires(this::checkPermission)
                        .then(
                                Commands.argument("locale", StringArgumentType.greedyString())
                                        .suggests(this::suggestLocale)
                                        .executes(this::dumpLocale)
                        )
        );

        super.registerAsChild(parentBuilder);
    }

    private int dumpLocale(CommandContext<CommandSourceStack> context)
    {
        var sender = context.getSource().getSender();
        var locale = StringArgumentType.getString(context, "locale");
        if (locale.equals("*"))
        {
            int result = 0;
            for (String s : this.validLocale)
                result += doExtract(sender, s);

            return result;
        }

        return doExtract(sender, locale);
    }

    private int doExtract(CommandSender sender, String locale)
    {
        var path = PluginAssetUtils.langPath(locale);
        var stringOptional = PluginAssetUtils.getFileStringsOptional(path);

        if (stringOptional.isEmpty())
        {
            MessageUtils.send(sender, CommandStrings.assetNotFound().resolve("type", TypesString.localeFile()));
            return 0;
        }

        var messagesDirectory = new File(plugin.getDataFolder(), "messages");

        try
        {
            var result = PluginAssetUtils.extractLocaleFile(locale, messagesDirectory, true);

            var msg = CommandStrings.dumpSuccess()
                    .resolve("what", locale)
                    .resolve("type", TypesString.localeFile())
                    .resolve("path", result.resultFile().getAbsolutePath());

            if (result.backupFile() != null)
                MessageUtils.send(sender, CommandStrings.oldFileRenamed().resolve("path", result.backupFile().getAbsolutePath()));

            MessageUtils.send(sender, msg);

            return 1;
        }
        catch (ExecutionErrorException e)
        {
            logger.error("Unable to dump file", e);
            MessageUtils.send(sender, CommandStrings.unableToWrite().resolve("path", messagesDirectory.getAbsolutePath()));
            return 0;
        }
    }

    private final List<String> validLocale =  PluginAssetUtils.allSupportedLanguages();

    private CompletableFuture<Suggestions> suggestLocale(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            var input = builder.getRemainingLowerCase();
            builder.suggest("*");
            validLocale.stream().filter(s -> s.contains(input)).forEach(builder::suggest);

            return builder.build();
        });
    }
}
