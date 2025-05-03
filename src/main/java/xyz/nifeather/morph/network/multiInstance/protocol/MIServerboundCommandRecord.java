package xyz.nifeather.morph.network.multiInstance.protocol;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import xyz.nifeather.morph.network.multiInstance.protocol.c2s.MIC2SCommand;

import java.util.Map;

public record MIServerboundCommandRecord(
        @Expose
        @SerializedName("command_name")
        String commandName,

        @Expose
        @SerializedName("argument_map")
        Map<String, String> arguments
)
{
    public static MIServerboundCommandRecord fromC2SCommand(MIC2SCommand command)
    {
        return new MIServerboundCommandRecord(command.getBaseName(), command.generateArgumentMap());
    }
}

