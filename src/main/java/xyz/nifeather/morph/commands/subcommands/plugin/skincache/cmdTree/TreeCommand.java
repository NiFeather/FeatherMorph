package xyz.nifeather.morph.commands.subcommands.plugin.skincache.cmdTree;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphPlugin;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TreeCommand implements ISubCommand
{
    @NotNull
    private final String cmdName;

    @Nullable
    private String permRequirement;

    @Nullable
    private Function<List<String>, List<String>> onTabComplete;

    @Nullable
    private BiFunction<CommandSender, List<String>, Boolean> executeFunction;

    @NotNull
    private FormattableMessage helpDescription = new FormattableMessage(MorphPlugin.getInstance(), "Nil");

    public TreeCommand(@NotNull String cmdName)
    {
        this.cmdName = cmdName;
    }

    @Override
    public @NotNull String getCommandName()
    {
        return cmdName;
    }

    private static final List<String> emptyList = ImmutableList.of();

    @Override
    public @Nullable List<String> onTabComplete(List<String> args, CommandSender source)
    {
        return (this.onTabComplete == null) ? emptyList : this.onTabComplete.apply(args);

        //return ISubCommand.super.onTabComplete(args, source);
    }

    @Override
    public @Nullable String getPermissionRequirement()
    {
        return this.permRequirement;
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return this.helpDescription;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        return this.executeFunction == null
                || this.executeFunction.apply(sender, Arrays.stream(args).toList());

        //return ISubCommand.super.onCommand(sender, args);
    }

    public static class TreeCommandBuilder
    {
        private static class BuildMeta
        {
            public String name;
            public Function<List<String>, List<String>> onTabComplete;
            public BiFunction<CommandSender, List<String>, Boolean> executes;
            public String perm;
            public FormattableMessage helpMessage;

            public boolean isValid()
            {
                return name != null && !name.isBlank();
            }

            public TreeCommand toCmd()
            {
                if (!isValid())
                    throw new IllegalStateException("Invalid Command Meta!");

                var instance = new TreeCommand(name);
                instance.onTabComplete = this.onTabComplete;
                instance.executeFunction = this.executes;
                instance.permRequirement = this.perm;
                instance.helpDescription = (this.helpMessage == null)
                        ? new FormattableMessage(MorphPlugin.getInstance(), "Nil")
                        : this.helpMessage;

                return instance;
            }
        }

        @Nullable
        private BuildMeta currentMeta;

        private final List<BuildMeta> metaList = new ObjectArrayList<>();

        public TreeCommandBuilder startNew()
        {
            if (currentMeta != null)
                metaList.add(currentMeta);

            currentMeta = new BuildMeta();

            return this;
        }

        public TreeCommandBuilder name(String name)
        {
            Objects.requireNonNull(currentMeta, "CurrentMeta is null!");

            if (name == null || name.isBlank())
                throw new IllegalArgumentException("Command name may not be blank or null");

            currentMeta.name = name;
            return this;
        }

        public TreeCommandBuilder onFilter(Function<List<String>, List<String>> func)
        {
            Objects.requireNonNull(currentMeta, "CurrentMeta is null!");

            currentMeta.onTabComplete = func;
            return this;
        }

        public TreeCommandBuilder executes(BiFunction<CommandSender, List<String>, Boolean> func)
        {
            Objects.requireNonNull(currentMeta, "CurrentMeta is null!");

            currentMeta.executes = func;
            return this;
        }

        public TreeCommandBuilder permission(String perm)
        {
            Objects.requireNonNull(currentMeta, "CurrentMeta is null!");

            currentMeta.perm = perm;
            return this;
        }

        public TreeCommandBuilder helpMessage(FormattableMessage msg)
        {
            Objects.requireNonNull(currentMeta, "CurrentMeta is null!");

            currentMeta.helpMessage = msg;
            return this;
        }

        public List<ISubCommand> buildAll()
        {
            if (currentMeta != null)
            {
                metaList.add(currentMeta);
                currentMeta = null;
            }

            var list = new ObjectArrayList<ISubCommand>();

            metaList.forEach(meta -> list.add(meta.toCmd()));

            return list;
        }
    }
}
