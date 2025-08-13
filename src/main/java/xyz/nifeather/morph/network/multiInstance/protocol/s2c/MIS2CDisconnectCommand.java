package xyz.nifeather.morph.network.multiInstance.protocol.s2c;

import xyz.nifeather.morph.network.multiInstance.protocol.IMasterHandler;
import xyz.nifeather.morph.network.utils.Asserts;

import java.util.Map;

public class MIS2CDisconnectCommand extends MIS2CCommand
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

    public static MIS2CDisconnectCommand fromArguments(Map<String, String> arguments) throws RuntimeException
    {
        return new MIS2CDisconnectCommand(
                Integer.parseInt(Asserts.getStringOrThrow(arguments, "code")),
                Asserts.getStringOrThrow(arguments, "detail")
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
}
