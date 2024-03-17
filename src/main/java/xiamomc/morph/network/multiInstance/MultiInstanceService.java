package xiamomc.morph.network.multiInstance;

import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.network.multiInstance.master.MasterInstance;
import xiamomc.morph.network.multiInstance.protocol.Operation;
import xiamomc.morph.network.multiInstance.protocol.c2s.MIC2SDisguiseMetaCommand;
import xiamomc.morph.network.multiInstance.protocol.s2c.MIS2CSyncMetaCommand;
import xiamomc.morph.network.multiInstance.slave.SlaveInstance;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.Arrays;
import java.util.UUID;

public class MultiInstanceService extends MorphPluginObject
{
    @Nullable
    private MasterInstance masterInstance;

    @Nullable
    private SlaveInstance slaveInstance;

    private void checkSanity()
    {
        if (masterInstance != null && slaveInstance != null)
            throw new IllegalStateException("Master instance and Slave instance are both not null, which is not good!");

        if (masterInstance == null && slaveInstance == null)
            throw new IllegalStateException("None of master or slave instance is alive!");

        if (isMaster.get() && masterInstance == null)
            throw new IllegalStateException("We are the master server, but the server instance is null?!");

        if (!isMaster.get() && slaveInstance == null)
            throw new IllegalStateException("We are the client, but the client instance is null?!");
    }

    private void prepareInstance(boolean isMaster)
    {
        logger.info("Preparing socket instance...");

        if (!stopAll())
        {
            logger.warn("Can't stop instance, not continuing...");

            masterInstance = null;
            slaveInstance = null;

            return;
        }

        masterInstance = null;
        slaveInstance = null;

        if (isMaster)
            masterInstance = new MasterInstance();
        else
            slaveInstance = new SlaveInstance();
    }

    private final Bindable<Boolean> isMaster = new Bindable<>(false);

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        configManager.bind(isMaster, ConfigOption.IS_MASTER);

        isMaster.onValueChanged((o, n) -> prepareInstance(n), true);
    }

    public void onDisable()
    {
        stopAll();
    }

    public void notifyDisguiseMetaChange(UUID uuid, Operation operation, String... identifiers)
    {
        checkSanity();

        if (isMaster.get())
        {
            assert masterInstance != null;
            masterInstance.broadcastCommand(new MIS2CSyncMetaCommand(operation, Arrays.stream(identifiers).toList(), uuid));
        }
        else
        {
            assert slaveInstance != null;
            slaveInstance.sendCommand(new MIC2SDisguiseMetaCommand(operation, Arrays.stream(identifiers).toList(), uuid));
        }
    }

    private boolean stopAll()
    {
        if (masterInstance != null) return masterInstance.stop();
        if (slaveInstance != null) return slaveInstance.stop();

        return true;
    }
}
