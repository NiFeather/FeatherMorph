package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

public class CreeperValues extends MonsterValues
{
    public final SingleValue<Integer> STATE = getSingle(0);
    public final SingleValue<Boolean> IS_CHARGED_CREEPER = getSingle(false);
    public final SingleValue<Boolean> IGNITED = getSingle(false);

    public CreeperValues()
    {
        registerSingle(STATE, IS_CHARGED_CREEPER, IGNITED);
    }
}
