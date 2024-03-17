package xiamomc.morph.network.multiInstance.slave;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.network.multiInstance.protocol.IMasterHandler;
import xiamomc.pluginbase.Managers.DependencyManager;
import xiamomc.pluginbase.XiaMoJavaPlugin;

import java.net.ConnectException;
import java.net.URI;

public class InstanceClient extends WebSocketClient
{
    private MorphConfigManager config;
    private Logger logger;

    private XiaMoJavaPlugin plugin;

    private IMasterHandler masterHandler;

    public InstanceClient(URI serverUri, XiaMoJavaPlugin plugin, IMasterHandler masterHandler)
    {
        super(serverUri);

        this.logger = plugin.getSLF4JLogger();

        var dependencies = DependencyManager.getManagerOrCreate(plugin);
        config = dependencies.get(MorphConfigManager.class, false);

        plugin.schedule(this::load);
        this.plugin = plugin;
        this.masterHandler = masterHandler;
    }

    private void load()
    {
    }

    //region WebSocket stuffs

    @Override
    public void onOpen(ServerHandshake serverHandshake)
    {
        logger.info("Opened connection to the instance server.");
        masterHandler.onConnectionOpen();
    }

    @Override
    public void onMessage(String msg)
    {
        //logger.info("Received server message: " + msg);
        masterHandler.onText(msg);
    }

    @Override
    public void onClose(int code, String reason, boolean isFromRemote)
    {
        logger.info("Connection closed with code '%s' and reason '%s'".formatted(code, reason));

        if (code == 1001 || code == 1000)
        {
            logger.info("Reconnecting after 10 seconds");
            plugin.schedule(this::reconnect, 10 * 20);
        }
    }

    @Override
    public void connect()
    {
        logger.info("Connecting the instance server...");
        super.connect();
    }

    @Override
    public void onError(Exception e)
    {
        if (e instanceof ConnectException)
        {
            logger.info("Can't reach the server, retrying after 5 seconds: " + e.getMessage());
            plugin.schedule(this::reconnect, 5 * 20);

            return;
        }

        logger.error("Unknown error occurred with the client: " + e.getMessage());
        e.printStackTrace();
    }

    //endregion
}
