package xiamomc.morph.network.multiInstance.master;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.network.multiInstance.protocol.IClientHandler;
import xiamomc.pluginbase.Managers.DependencyManager;
import xiamomc.pluginbase.XiaMoJavaPlugin;

import java.net.InetSocketAddress;
import java.util.List;

public final class InstanceServer extends WebSocketServer
{
    private final Logger logger;

    @Nullable
    private MorphConfigManager config;

    private IClientHandler clientHandler;

    public InstanceServer(XiaMoJavaPlugin plugin, InetSocketAddress address, IClientHandler iClientHandler)
    {
        super(address);

        this.logger = plugin.getSLF4JLogger();

        var dependencies = DependencyManager.getManagerOrCreate(plugin);
        config = dependencies.get(MorphConfigManager.class, false);

        this.clientHandler = iClientHandler;

        plugin.schedule(this::load);
    }

    private void load()
    {
    }

    private final List<WebSocket> connectedSockets = new ObjectArrayList<>();

    public List<WebSocket> getConnectedSockets()
    {
        return new ObjectArrayList<>(connectedSockets);
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake)
    {
        logger.info("New connection opened: " + webSocket.getRemoteSocketAddress());

        connectedSockets.add(webSocket);
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b)
    {
        logger.info("Connection closed: " + webSocket.getRemoteSocketAddress());

        connectedSockets.remove(webSocket);
    }

    public record WsRecord(WebSocket socket, String rawMessage)
    {
    }

    @Override
    public void onMessage(WebSocket webSocket, String msg)
    {
        logger.info("%s :: <- :: '%s'".formatted(webSocket.getRemoteSocketAddress(), msg));

        clientHandler.onMessage(new WsRecord(webSocket, msg), this);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e)
    {
        logger.warn("An error occurred with socket '%s': %s".formatted(webSocket.getRemoteSocketAddress(), e.getMessage()));
        e.printStackTrace();
    }

    @Override
    public void onStart()
    {
        logger.info("Master websocket server started on " + this.getAddress().toString());
    }

    public void dispose()
    {
    }
}
