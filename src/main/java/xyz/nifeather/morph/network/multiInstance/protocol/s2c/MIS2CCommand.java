package xyz.nifeather.morph.network.multiInstance.protocol.s2c;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.java_websocket.WebSocket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.network.multiInstance.protocol.IMasterHandler;

import java.util.Map;

public abstract class MIS2CCommand
{
    //region Utilities

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    protected static Gson gson()
    {
        return gson;
    }

    public abstract Map<String, String> generateArgumentMap();

    //endregion Utilities

    protected final Logger logger = FeatherMorphMain.getInstance().getSLF4JLogger();

    protected final String baseName;

    public String getBaseName()
    {
        return baseName;
    }

    public abstract void onCommand(IMasterHandler handler);

    public MIS2CCommand(String cmdBaseName)
    {
        this.baseName = cmdBaseName;
    }

    @Nullable
    private WebSocket sourceSocket;

    public void setSourceSocket(@NotNull WebSocket socket)
    {
        this.sourceSocket = socket;
    }

    @Nullable
    public WebSocket getSocket()
    {
        return sourceSocket;
    }
}
