package xyz.nifeather.netherite;

import org.jetbrains.annotations.Nullable;
import xyz.nifeather.netherite.network.BasicClientHandler;
import xyz.nifeather.netherite.network.InitializeState;
import xyz.nifeather.netherite.network.PlayerOptions;
import xyz.nifeather.netherite.network.commands.C2S.*;
import xyz.nifeather.netherite.network.commands.CommandRegistries;
import xyz.nifeather.netherite.network.commands.S2C.NetheriteS2CCommand;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class LegacyCommandProcessor<TPlayer> implements BasicClientHandler<TPlayer>
{
    private final CommandRegistries registries = new CommandRegistries();

    private final Logger logger = Logger.getLogger("FeatherMorph - Netherite");

    public LegacyCommandProcessor()
    {
        registries.registerC2S(NetheriteC2SCommandNames.Initial, a -> new NetheriteC2SInitialCommand())
                .registerC2S(NetheriteC2SCommandNames.Morph, NetheriteC2SMorphCommand::new)
                .registerC2S(NetheriteC2SCommandNames.Skill, a -> new NetheriteC2SSkillCommand())
                .registerC2S(NetheriteC2SCommandNames.Option, NetheriteC2SOptionCommand::fromString)
                .registerC2S(NetheriteC2SCommandNames.ToggleSelf, a -> new NetheriteC2SToggleSelfCommand(NetheriteC2SToggleSelfCommand.SelfViewMode.fromString(a)))
                .registerC2S(NetheriteC2SCommandNames.Unmorph, a -> new NetheriteC2SUnmorphCommand())
                .registerC2S(NetheriteC2SCommandNames.Request, NetheriteC2SRequestCommand::new)
                .registerC2S("animation", NetheriteC2SAnimationCommand::new);
    }

    /**
     * @param owner
     * @param commandLine
     * @return Whether handle success
     */
    public NetheriteC2SCommand<?> processLegacyCommandLine(TPlayer owner, String commandLine) throws RuntimeException
    {
        var str = commandLine.split(" ", 2);

        if (str.length < 1)
            throw new RuntimeException("Incomplete server command: " + commandLine);

        var baseCommand = str[0];
        var c2sCommand = registries.createC2SCommand(baseCommand, str.length == 2 ? str[1] : "");

        if (c2sCommand == null)
            throw new RuntimeException("Unknown server command for name '%s'".formatted(baseCommand));

        c2sCommand.setOwner(owner);
        return c2sCommand;
    }

    private final Map<HandleType<?>, Consumer<?>> handles = new ConcurrentHashMap<>();

    public <X> void setHandle(HandleType<X> handleType, Consumer<X> consumer)
    {
        handles.put(handleType, consumer);
    }

    public <X> void processHandle(HandleType<X> handleType, X commandInstance)
    {
        var handle = (Consumer<Object>) handles.getOrDefault(handleType, null);
        if (handle != null) handle.accept(commandInstance);
    }

    @Override
    public void onInitialCommand(NetheriteC2SInitialCommand netheriteC2SInitialCommand)
    {
        this.processHandle(HandleType.REQUEST_INITIAL, netheriteC2SInitialCommand);
    }

    @Override
    public void onMorphCommand(NetheriteC2SMorphCommand netheriteC2SMorphCommand)
    {
        this.processHandle(HandleType.REQUEST_MORPH, netheriteC2SMorphCommand);
    }

    @Override
    public void onOptionCommand(NetheriteC2SOptionCommand netheriteC2SOptionCommand)
    {
        this.processHandle(HandleType.SET_SINGLE_OPTION, netheriteC2SOptionCommand);
    }

    @Override
    public void onSkillCommand(NetheriteC2SSkillCommand netheriteC2SSkillCommand)
    {
        this.processHandle(HandleType.ACTIVATE_SKILL, netheriteC2SSkillCommand);
    }

    @Override
    public void onToggleSelfCommand(NetheriteC2SToggleSelfCommand netheriteC2SToggleSelfCommand)
    {
        this.processHandle(HandleType.TOGGLE_SELF_VIEW, netheriteC2SToggleSelfCommand);
    }

    @Override
    public void onUnmorphCommand(NetheriteC2SUnmorphCommand netheriteC2SUnmorphCommand)
    {
        this.processHandle(HandleType.REQUEST_UNMORPH, netheriteC2SUnmorphCommand);
    }

    @Override
    public void onRequestCommand(NetheriteC2SRequestCommand netheriteC2SRequestCommand)
    {
        this.processHandle(HandleType.PROCESS_EXCHANGE_REQUEST, netheriteC2SRequestCommand);
    }

    @Override
    public void onAnimationCommand(NetheriteC2SAnimationCommand netheriteC2SAnimationCommand)
    {
        this.processHandle(HandleType.PLAY_ANIMATION, netheriteC2SAnimationCommand);
    }

    //region Never Implement

    public static class NotImplementedException extends RuntimeException
    {
        public NotImplementedException(String message)
        {
            super(message);
        }
    }

    @Override
    public int getPlayerVersion(TPlayer player)
    {
        throw new NotImplementedException("getPlayerVersion is not implemented for LegacyCommandHub");
    }

    @Override
    public List<TPlayer> getConnectedPlayers()
    {
        throw new NotImplementedException("getConnectedPlayers is not implemented for LegacyCommandHub");
    }

    @Override
    public InitializeState getInitializeState(TPlayer player)
    {
        throw new NotImplementedException("getInitializeState is not implemented for LegacyCommandHub");
    }

    @Override
    public boolean isPlayerInitialized(TPlayer player)
    {
        throw new NotImplementedException("isPlayerInitialized is not implemented for LegacyCommandHub");
    }

    @Override
    public boolean isPlayerConnected(TPlayer player)
    {
        throw new NotImplementedException("isPlayerConnected is not implemented for LegacyCommandHub");
    }

    @Override
    public void disconnect(TPlayer player)
    {
        throw new NotImplementedException("disconnect is not implemented for LegacyCommandHub");
    }

    @Override
    public @Nullable PlayerOptions<TPlayer> getPlayerOption(TPlayer player)
    {
        throw new NotImplementedException("getPlayerOption is not implemented for LegacyCommandHub");
    }

    @Override
    public boolean sendCommand(TPlayer player, NetheriteS2CCommand<?> netheriteS2CCommand)
    {
        throw new NotImplementedException("sendCommand is not implemented for LegacyCommandHub");
    }

    //endregion Never Implement
}
