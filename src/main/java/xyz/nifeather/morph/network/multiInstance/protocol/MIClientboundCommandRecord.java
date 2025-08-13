package xyz.nifeather.morph.network.multiInstance.protocol;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import xyz.nifeather.morph.network.multiInstance.protocol.s2c.MIS2CCommand;

import java.util.Map;

public record MIClientboundCommandRecord(
        @Expose
        @SerializedName("command_name")
        String commandName,

        @Expose
        @SerializedName("argument_map")
        Map<String, String> arguments
)
{
    public static MIClientboundCommandRecord fromS2CCommand(MIS2CCommand command)
    {
        return new MIClientboundCommandRecord(command.getBaseName(), command.generateArgumentMap());
    }
}

