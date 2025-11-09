package xyz.nifeather.morph.commands.subcommands.plugin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.messages.strings.TypesString;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.utilities.PluginAssetUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
        return "dump_language";
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
                                Commands.argument("locale", StringArgumentType.word())
                                        .suggests(this::suggestLocale)
                                        .executes(this::dumpLocale)
                        )
        );

        super.registerAsChild(parentBuilder);
    }

    private int dumpLocale(CommandContext<CommandSourceStack> context)
    {
        var locale = StringArgumentType.getString(context, "locale");
        var path = PluginAssetUtils.langPath(locale);
        var stringOptional = PluginAssetUtils.getFileStringsOptional(path);

        var sender = context.getSource().getSender();

        if (stringOptional.isEmpty())
        {
            MessageUtils.send(sender, CommandStrings.assetNotFound().resolve("type", TypesString.localeFile()));
            return 0;
        }

        var messagesDirectory = new File(plugin.getDataFolder(), "messages");
        var localeFile = new File(messagesDirectory, "%s.json".formatted(locale));
        if (localeFile.exists())
        {
            MessageUtils.send(sender, CommandStrings.targetAlreadyExists().resolve("path", localeFile.getAbsolutePath()));
            return 0;
        }

        try
        {
            if (!messagesDirectory.exists())
                Files.createDirectory(messagesDirectory.toPath());

            Files.writeString(localeFile.toPath(), stringOptional.get(), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            MessageUtils.send(sender, CommandStrings.unableToWrite().resolve("path", localeFile.getAbsolutePath()));
            logger.info("Failed to dump locale file", e);
            return 0;
        }

        var msg = CommandStrings.dumpSuccess()
                .resolve("what", locale)
                .resolve("type", TypesString.localeFile())
                .resolve("path", localeFile.getAbsolutePath());
        MessageUtils.send(sender, msg);

        return 1;
    }

    private CompletableFuture<Suggestions> suggestLocale(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder)
    {
        return builder.buildFuture();
    }
}
