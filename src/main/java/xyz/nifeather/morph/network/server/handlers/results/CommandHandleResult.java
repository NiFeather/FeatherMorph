package xyz.nifeather.morph.network.server.handlers.results;

import xyz.nifeather.morph.network.commands.C2S.C2SCommandRecord;

import java.util.List;
import java.util.Map;

public record CommandHandleResult(boolean success, C2SCommandRecord result)
{
    private static final CommandHandleResult resultFailed = new CommandHandleResult(false, new C2SCommandRecord("failed", Map.of()));

    public static CommandHandleResult fail()
    {
        return resultFailed;
    }

    public static CommandHandleResult from(C2SCommandRecord input)
    {
        return new CommandHandleResult(true, input);
    }
}
