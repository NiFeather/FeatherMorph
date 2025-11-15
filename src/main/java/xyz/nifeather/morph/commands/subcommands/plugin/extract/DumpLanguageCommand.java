package xyz.nifeather.morph.commands.subcommands.plugin.extract;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;
import xyz.nifeather.morph.commands.brigadier.arguments.RelaxedStringArgumentType;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.messages.strings.OperationStrings;
import xyz.nifeather.morph.messages.strings.TypesString;
import xyz.nifeather.morph.misc.ConfirmationHandler;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.utilities.PluginAssetUtils;

import java.io.File;
import java.time.Duration;
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

    private final List<String> validLocale;

    private final ConfirmationHandler<CommandSender> confirmationHandler = new ConfirmationHandler<>();

    public DumpLanguageCommand()
    {
        ImmutableList.Builder<String> builder = ImmutableList.builder();

        builder.addAll(PluginAssetUtils.allSupportedLanguages())
                .add("*");

        validLocale = builder.build();

        confirmationHandler.onSubmit().hook((sender, duration) ->
        {
            var msg = CommandStrings.confirmationRequired()
                    .resolve("second", duration.getSeconds())
                    .resolve("operation", OperationStrings.overwritingOneOrMore().resolve("type", TypesString.localeFile()));

            MessageUtils.send(sender, msg);
        });

        confirmationHandler.onExpire().hook((sender) ->
        {
            var msg = CommandStrings.confirmationExpired()
                            .resolve("operation", OperationStrings.overwritingOneOrMore().resolve("type", TypesString.localeFile()));

            MessageUtils.send(sender, msg);
        });

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(FeatherMorphMain.getInstance(), task ->
        {
            confirmationHandler.update();
        }, 1, 10);
    }

    @Override
    public void registerAsChild(ArgumentBuilder<CommandSourceStack, ?> parentBuilder)
    {
        parentBuilder.then(
                Commands.literal(name())
                        .requires(this::checkPermission)
                        .then(
                                Commands.argument("locale", RelaxedStringArgumentType.relaxed())
                                        .suggests(this::suggestLocale)
                                        .executes(c -> dumpLocale(c, false))
                                        .then(
                                                Commands.argument("overwrite_existing", BoolArgumentType.bool())
                                                        .executes(c -> dumpLocale(c, BoolArgumentType.getBool(c, "overwrite_existing")))
                                        )
                        )
        );

        super.registerAsChild(parentBuilder);
    }

    private CompletableFuture<Suggestions> suggestLocale(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            var input = builder.getRemainingLowerCase();
            validLocale.stream().filter(s -> s.contains(input)).forEach(builder::suggest);

            return builder.build();
        });
    }

    private int dumpLocale(CommandContext<CommandSourceStack> context, boolean overwrite)
    {
        var sender = context.getSource().getSender();

        // If the user is trying to overwrite, make sure that they confirm the action
        if (overwrite)
        {
            if (!confirmationHandler.confirm(sender))
            {
                confirmationHandler.submit(sender, Duration.ofSeconds(2));
                return 0;
            }
        }

        var locale = StringArgumentType.getString(context, "locale");
        if (locale.equals("*"))
        {
            int result = 0;
            for (String s : PluginAssetUtils.allSupportedLanguages())
                result += doExtract(sender, s, overwrite);

            return result;
        }

        return doExtract(sender, locale, overwrite);
    }

    private int doExtract(CommandSender sender, String locale, boolean overwriteExisting)
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
            var result = PluginAssetUtils.extractLocaleFile(locale, messagesDirectory, overwriteExisting);

            var msg = CommandStrings.dumpSuccess()
                    .resolve("what", locale)
                    .resolve("type", TypesString.localeFile())
                    .resolve("path", result.resultFile().getAbsolutePath());

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
}
