package xyz.nifeather.morph.network.server;

import org.jetbrains.annotations.ApiStatus;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.network.BasicServerHandler;
import xyz.nifeather.morph.network.commands.S2C.AbstractS2CCommand;

import java.util.List;
import java.util.Map;

@ApiStatus.Experimental
public class S2CDiscardPropertiesCommand extends AbstractS2CCommand<String>
{
    private final List<String> propertyNames;

    public List<String> propertyNames()
    {
        return List.copyOf(propertyNames);
    }

    public S2CDiscardPropertiesCommand(List<String> properties)
    {
        this.propertyNames = List.copyOf(properties);
    }

    @Override
    public String getBaseName()
    {
        return "discard_properties";
    }

    @Override
    public void onCommand(BasicServerHandler<?> basicServerHandler)
    {
    }

    @Override
    public Map<String, String> generateArgumentMap()
    {
        return Map.of(
                "properties", gson().toJson(propertyNames)
        );
    }
}
