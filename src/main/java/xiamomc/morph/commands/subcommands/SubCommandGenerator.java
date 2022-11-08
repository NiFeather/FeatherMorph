package xiamomc.morph.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.messages.FormattableMessage;

import java.util.function.BiFunction;

public class SubCommandGenerator
{
    public static GeneratedCommand command(String name, BiFunction<CommandSender, String[], Boolean> execute)
    {
        return command(name, null, execute);
    }

    public static GeneratedCommand command(String name, String permission, BiFunction<CommandSender, String[], Boolean> execute)
    {
        return new GeneratedCommand(name, permission, execute);
    }

    public static GeneratedCommand command()
    {
        return new GeneratedCommand();
    }

    public static class GeneratedCommand extends MorphPluginObject implements ISubCommand
    {
        public GeneratedCommand(String name, BiFunction<CommandSender, String[], Boolean> exec)
        {
            this(name, null, exec);
        }

        public GeneratedCommand(String name, String permission, BiFunction<CommandSender, String[], Boolean> exec)
        {
            this.name = name;
            this.perm = permission;
            this.exec = exec;
        }

        public GeneratedCommand()
        {
        }

        private String name = "???";
        private BiFunction<CommandSender, String[], Boolean> exec;
        private String perm;

        public GeneratedCommand setName(String name)
        {
            this.name = name;
            return this;
        }

        public GeneratedCommand setExec(BiFunction<CommandSender, String[], Boolean> exec)
        {
            this.exec = exec;
            return this;
        }

        public GeneratedCommand setPerm(String perm)
        {
            this.perm = perm;
            return this;
        }

        @Override
        public @Nullable String getPermissionRequirement()
        {
            return perm;
        }

        @Override
        public @NotNull String getCommandName()
        {
            return name;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
        {
            if (exec != null) return exec.apply(sender, args);
            else return false;
        }

        @Override
        public FormattableMessage getHelpMessage()
        {
            return new FormattableMessage(plugin, name);
        }
    }
}
