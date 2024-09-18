package xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import xiamomc.morph.backends.server.renderer.network.datawatcher.values.MonsterValues;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

public class BasePiglinValues extends MonsterValues
{
    public final SingleValue<Boolean> IMMUNE_TO_ZOMBIFICATION = createSingle("piglin_immune_to_zombification", true);

    public BasePiglinValues()
    {
        super();

        registerSingle(IMMUNE_TO_ZOMBIFICATION);
    }
}
