package xyz.nifeather.morph.network.multiInstance.protocol.s2c;

import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.network.multiInstance.protocol.IMasterHandler;

import java.util.Map;

public class MIS2CDisconnectCommand extends MIS2CCommand<String>
{
    private final int reasonCode;
    private final String detail;

    public MIS2CDisconnectCommand(int reasonCode)
    {
        this(reasonCode, "<No details>");
    }

    public MIS2CDisconnectCommand(int reasonCode, String detail)
    {
        super("deny");
        this.reasonCode = reasonCode;
        this.detail = detail;
    }

    @Override
    public Map<String, String> generateArgumentMap()
    {
        return Map.of(
                "code", Integer.toString(reasonCode),
                "detail", detail
        );
    }

    @Override
    public void onCommand(IMasterHandler handler)
    {
        handler.onDisconnectCommand(this);
    }

    public int getReasonCode()
    {
        return reasonCode;
    }

    public String getDetails()
    {
        return detail;
    }

    public static MIS2CDisconnectCommand from(String text)
    {
        var args = text.split(" ", 2);
        int reasonCode = -2;

        try
        {
            reasonCode = Integer.parseInt(args[0]);
        }
        catch (Throwable t)
        {
            var logger = FeatherMorphMain.getInstance().getSLF4JLogger();

            logger.warn("Can't parse disconnect reason code from the server command");
        }

        if (args.length == 2)
            return new MIS2CDisconnectCommand(reasonCode, args[1]);
        else
            return new MIS2CDisconnectCommand(reasonCode);
    }
}
