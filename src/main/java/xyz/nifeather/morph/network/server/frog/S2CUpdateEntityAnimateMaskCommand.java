package xyz.nifeather.morph.network.server.frog;

import xyz.nifeather.morph.network.BasicServerHandler;
import xyz.nifeather.morph.network.commands.S2C.AbstractS2CCommand;
import xyz.nifeather.morph.network.utils.Asserts;

import java.util.Map;

public class S2CUpdateEntityAnimateMaskCommand extends AbstractS2CCommand<String>
{
    private final boolean allowed;

    public boolean isAllowed()
    {
        return allowed;
    }

    private final String animateName;

    public String animateName()
    {
        return animateName;
    }

    public S2CUpdateEntityAnimateMaskCommand(String animateName, boolean allowed)
    {
        this.allowed = allowed;
        this.animateName = animateName;
    }

    @Override
    public String getBaseName()
    {
        return "frog_update_entity_animate_mask";
    }

    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
    }

    @Override
    public Map<String, String> generateArgumentMap()
    {
        return Map.of(
                "allow", isAllowed() + "",
                "name", animateName()
        );
    }

    public static S2CUpdateEntityAnimateMaskCommand fromArguments(Map<String, String> argumentMap)
    {
        var allow = Asserts.getStringOrThrow(argumentMap, "allow");
        var animName = Asserts.getStringOrThrow(argumentMap, "name");

        return new S2CUpdateEntityAnimateMaskCommand(animName, Boolean.parseBoolean(allow));
    }
}
