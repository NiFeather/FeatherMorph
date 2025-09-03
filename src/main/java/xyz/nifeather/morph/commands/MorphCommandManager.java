package xyz.nifeather.morph.commands;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.commands.brigadier.IConvertibleBrigadier;

import java.io.File;
import java.time.Duration;
import java.util.List;

public class MorphCommandManager extends MorphPluginObject
{
    private final List<IConvertibleBrigadier> commands = List.of(
            new MorphCommand(),
            new MorphPlayerCommand(),
            new UnMorphCommand(),
            new RequestCommand(),
            new MorphPluginCommand(),
            new AnimationCommand());

    public List<IConvertibleBrigadier> commands()
    {
        return this.commands;
    }

    public void register(ReloadableRegistrarEvent<@NotNull Commands> event)
    {
        var registrar = event.registrar();

        for (var brigadierConvertable : commands)
            brigadierConvertable.register(registrar);

        var cmdDebugFile = new File(FeatherMorphMain.getInstance().getDataFolder(), "cmd_debug.txt");
        if (cmdDebugFile.exists())
        {
            logger.error("- x - x - x - x - x - x - x - x - x - x - x - x -");
            logger.error("MAY I HAVE YOUR ATTENTION PLEASE!");
            logger.error("");
            logger.error("Debug commands are enabled, these commands are only meant to do debugging stuffs!");
            logger.error("These commands may have bugs, or incomplete permission check that allows everyone to execute, even crash the server!");
            logger.error("Please make sure that you REALLY need these commands; if not, please remove file [%s] IMMEDIATELY and RESTART THE SERVER!".formatted(cmdDebugFile.getAbsolutePath()));
            logger.error("");
            logger.error("- x - x - x - x - x - x - x - x - x - x - x - x -");

            try
            {
                Thread.sleep(Duration.ofSeconds(3));
            }
            catch (InterruptedException ignored)
            {
            }

            new DebugTestCommand().register(registrar);
        }
    }
}
