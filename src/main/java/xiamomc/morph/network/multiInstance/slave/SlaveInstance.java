package xiamomc.morph.network.multiInstance.slave;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.network.ReasonCodes;
import xiamomc.morph.network.commands.C2S.AbstractC2SCommand;
import xiamomc.morph.network.commands.CommandRegistries;
import xiamomc.morph.network.multiInstance.IInstanceService;
import xiamomc.morph.network.multiInstance.protocol.IMasterHandler;
import xiamomc.morph.network.multiInstance.protocol.Operation;
import xiamomc.morph.network.multiInstance.protocol.ProtocolLevel;
import xiamomc.morph.network.multiInstance.protocol.c2s.MIC2SCommand;
import xiamomc.morph.network.multiInstance.protocol.c2s.MIC2SDisguiseMetaCommand;
import xiamomc.morph.network.multiInstance.protocol.c2s.MIC2SLoginCommand;
import xiamomc.morph.network.multiInstance.protocol.s2c.*;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.morph.storage.playerdata.PlayerMeta;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class SlaveInstance extends MorphPluginObject implements IInstanceService, IMasterHandler
{
    @Nullable
    private InstanceClient client;

    private boolean stopClient()
    {
        if (client == null) return true;

        try
        {
            client.close(ReasonCodes.DISCONNECT);
            client = null;

            return true;
        }
        catch (Throwable t)
        {
            logger.warn("Can't close client!");
            return false;
        }
    }

    @Resolved
    private MorphConfigManager config;

    /**
     * @return Whether this operation operates successfully
     */
    private boolean prepareClient()
    {
        if (!stopClient())
            return false;

        try
        {
            var rawAddr = config.getOrDefault(String.class, ConfigOption.MASTER_ADDRESS);
            var uri = URI.create("ws://" + rawAddr);

            var client = new InstanceClient(uri, plugin, this);

            this.client = client;
            CompletableFuture.runAsync(client);

            return true;
        }
        catch (Throwable t)
        {
            logger.warn("Error occurred setting up client: " + t.getMessage());
            t.printStackTrace();

            return false;
        }
    }

    @Initializer
    private void load()
    {
        logger.info("Preparing multi-instance service...");

        config.bind(secret, ConfigOption.MASTER_SECRET);

        registries.registerS2C("deny", MIS2CDisconnectCommand::from)
                .registerS2C("dmeta", MIS2CDisguiseMetaCommand::from)
                .registerS2C("r_login", MIS2CLoginResultCommand::from);

        if (!prepareClient())
        {
            logger.warn("Can't setup client, this instance will stay offline from the instance network!");
            return;
        }
    }

    private final Bindable<String> secret = new Bindable<>(null);

    @Override
    public boolean stop()
    {
        return stopClient();
    }

    @Resolved
    private MorphManager morphManager;

    @Resolved
    private MorphClientHandler clientHandler;

    @ApiStatus.Internal
    public void sendCommand(AbstractC2SCommand<?> command)
    {
        if (silent)
            return;

        if (client == null)
            throw new NullDependencyException("Null client!");

        client.send(command.buildCommand());
    }

    @Override
    public void onDisguiseMetaCommand(MIS2CDisguiseMetaCommand metaCommand)
    {
        var meta = metaCommand.getMeta();
        if (meta == null)
        {
            logger.warn("Bad server implementation? Get DisguiseMeta command but meta is null!");
            return;
        }

        if (!meta.isValid())
        {
            logger.warn("Bad server implementation? The meta is invalid!");
            return;
        }

        var operation = meta.getOperation();
        var offlinePlayer = Bukkit.getOfflinePlayer(Objects.requireNonNull(meta.getBindingUuid(), "???"));

        var playerMeta = morphManager.getPlayerMeta(offlinePlayer);
        var unlocked = playerMeta.getUnlockedDisguises();

        var player = offlinePlayer.getPlayer();

        silent = true;

        if (operation == Operation.ADD_IF_ABSENT)
        {
            meta.getIdentifiers().forEach(id ->
            {
                var disguiseMeta = morphManager.getDisguiseMeta(id);

                if (!unlocked.contains(disguiseMeta))
                    playerMeta.addDisguise(disguiseMeta);
            });

            //morphManager.saveConfiguration();

            if (player != null)
                clientHandler.refreshPlayerClientMorphs(playerMeta.getUnlockedDisguiseIdentifiers(), player);
        }
        else if (operation == Operation.REMOVE)
        {
            meta.getIdentifiers().forEach(id ->
            {
                var disguiseMeta = morphManager.getDisguiseMeta(id);

                playerMeta.removeDisguise(disguiseMeta);
            });

            //morphManager.saveConfiguration();

            if (player != null)
                clientHandler.refreshPlayerClientMorphs(playerMeta.getUnlockedDisguiseIdentifiers(), player);
        }

        silent = false;
    }

    @Override
    public void onDisconnectCommand(MIS2CDisconnectCommand cDenyCommand)
    {
        this.stopClient();
    }

    @Override
    public void onLoginResultCommand(MIS2CLoginResultCommand cLoginResultCommand)
    {
        if (!cLoginResultCommand.isAllowed()) return;

        var cmds = new ObjectArrayList<MIC2SDisguiseMetaCommand>();
        var disguises = morphManager.listAllPlayerMeta();
        for (var meta : disguises)
        {
            var identifiers = meta.getUnlockedDisguiseIdentifiers();

            if (!identifiers.isEmpty())
                cmds.add(new MIC2SDisguiseMetaCommand(Operation.ADD_IF_ABSENT, identifiers, meta.uniqueId));
        }

        cmds.forEach(this::sendCommand);
    }

    private final ProtocolLevel implementingLevel = ProtocolLevel.V1;

    @Override
    public void onConnectionOpen()
    {
        this.addSchedule(() -> this.sendCommand(new MIC2SLoginCommand(implementingLevel, secret.get())));
    }

    private final CommandRegistries registries = new CommandRegistries();

    @Override
    public void onText(String text)
    {
        this.addSchedule(() -> onCommandRaw(text));
    }

    private void onCommandRaw(String raw)
    {
        var text = raw.split(" ", 2);
        var cmd = registries.createS2CCommand(text[0], text.length == 2 ? text[1] : "");
        if (cmd == null)
        {
            logger.warn("Unknown command: " + text[0]);
            return;
        }

        if (!(cmd instanceof MIS2CCommand<?> mis2c))
        {
            logger.warn("Command is not a MIS2C instance!");
            return;
        }

        mis2c.onCommand(this);
    }

    private boolean silent = false;
}
