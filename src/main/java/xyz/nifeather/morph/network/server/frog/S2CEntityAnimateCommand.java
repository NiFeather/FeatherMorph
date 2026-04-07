package xyz.nifeather.morph.network.server.frog;

import org.jetbrains.annotations.ApiStatus;
import xyz.nifeather.morph.network.BasicServerHandler;
import xyz.nifeather.morph.network.commands.S2C.AbstractS2CCommand;
import xyz.nifeather.morph.network.utils.Asserts;

import java.util.Map;

@ApiStatus.Experimental
public class S2CEntityAnimateCommand extends AbstractS2CCommand<Integer>
{
    private final String animateName;

    public String animateName()
    {
        return animateName;
    }

    public S2CEntityAnimateCommand(String animateName)
    {
        this.animateName = animateName;
    }

    @Override
    public String getBaseName()
    {
        return "entity_animate_v0";
    }

    @Override
    public void onCommand(BasicServerHandler<?> basicServerHandler)
    {
    }

    @Override
    public Map<String, String> generateArgumentMap()
    {
        return Map.of(
                "id", animateName
        );
    }

    public static S2CEntityAnimateCommand fromArguments(Map<String, String> argumentMap)
    {
        var name = Asserts.getStringOrThrow(argumentMap, "id");
        return new S2CEntityAnimateCommand(name);
    }

    public static final String ANIM_SWING_MAINHAND = "sw_mainhand";
    public static final String ANIM_SWING_OFFHAND = "sw_offhand";
}
